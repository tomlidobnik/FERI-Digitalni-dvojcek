use crate::handlers::web_socket_handler::{handle_ws, Connections};
use crate::routers::{event_router, location_router, user_router};
use axum::{Router, routing::get};
use log::info;
use pretty_env_logger;
use std::net::SocketAddr;
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::Mutex;
use tower_http::cors::{Any, CorsLayer};

pub async fn create_router(connections: Connections) -> Router {
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any);

    let ws_routes = Router::new().route("/", get({
        let connections = connections.clone();
        move |ws, connect_info| handle_ws(ws, connect_info, connections.clone())
    }));

    let api_routes = Router::new()
        .nest("/user", user_router::create_router())
        .nest("/event", event_router::create_router())
        .nest("/location", location_router::create_router());

    Router::new()
        .nest("/ws", ws_routes)
        .nest("/api", api_routes)
        .layer(cors)
}

pub async fn run_server() {
    pretty_env_logger::init();
    let addr: SocketAddr = "0.0.0.0:8000".parse().unwrap();
    info!("Server running at http://{}", addr);

    let connections: Connections = Arc::new(Mutex::new(HashMap::new()));
    let app = create_router(connections).await;

    axum::serve(
        tokio::net::TcpListener::bind(addr).await.unwrap(),
        app.into_make_service_with_connect_info::<SocketAddr>(),
    )
    .await
    .unwrap();
}
