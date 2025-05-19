use crate::handlers::web_socket_handler::{handle_ws_event,handle_ws_friend, EventConnections, FriendConnections};
use crate::routers::{event_router, location_router, user_router,chat_router, location_outline_router};
use axum::{Router, routing::get};
use log::info;
use pretty_env_logger;
use std::net::SocketAddr;
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::Mutex;
use tower_http::cors::{Any, CorsLayer};
use axum_server::tls_rustls::RustlsConfig;

pub async fn create_router(friend_connections: FriendConnections, event_connections:EventConnections) -> Router {
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any).allow_headers(Any);

    let ws_event_route = Router::new().route(
        "/event/{event_id}",
        get({
            let connections = event_connections.clone();
            move |path, ws, connect_info| handle_ws_event(path, ws, connect_info, connections.clone())
        }),
    );

    let ws_friend_route = Router::new().route(
        "/friend/{friend_id}",
        get({
            let connections = friend_connections.clone();
            move |path, ws, connect_info| handle_ws_friend(path, ws, connect_info, connections.clone())
        }),
    );

    let ws_routes = Router::new()
        .merge(ws_event_route)
        .merge(ws_friend_route);

    let api_routes = Router::new()
        .nest("/user", user_router::create_router().layer(cors.clone()))
        .nest("/event", event_router::create_router().layer(cors.clone()))
        .nest("/location", location_router::create_router().layer(cors.clone()))
        .nest("/location_outline", location_outline_router::create_router().layer(cors.clone()))
        .nest("/chat", chat_router::create_router().layer(cors.clone()));

    Router::new()
        .nest("/ws", ws_routes)
        .nest("/api", api_routes)
        .layer(cors)
}

pub async fn run_server() {
    pretty_env_logger::init();
    let addr: SocketAddr = "0.0.0.0:8000".parse().unwrap();
    info!("Server running at http://{}", addr);

    let friend_connections: FriendConnections = Arc::new(Mutex::new(HashMap::new()));
    let event_connections: EventConnections = Arc::new(Mutex::new(HashMap::new()));
    let app = create_router(friend_connections,event_connections).await;
    let config = RustlsConfig::from_pem_file("cert.pem", "key.pem").await.unwrap();

    axum_server::bind_rustls(addr, config)
        .serve(app.into_make_service_with_connect_info::<SocketAddr>())
        .await
        .unwrap();
}
