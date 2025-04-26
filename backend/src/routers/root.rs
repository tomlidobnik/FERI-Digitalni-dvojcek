use crate::routers::user_router;
use axum::Router;
use log::info;
use pretty_env_logger;
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};

use super::event_router;

pub async fn create_router() -> Router {
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any);
    let api_routes = Router::new()
        .nest("/user", user_router::create_router())
        .nest("/event", event_router::create_router());
    Router::new().nest("/api", api_routes).layer(cors)
}

pub async fn run_server() {
    pretty_env_logger::init();
    let addr: SocketAddr = "0.0.0.0:8000".parse().unwrap();
    info!("Server running at http://{}", addr);
    let app = create_router().await;
    axum::serve(tokio::net::TcpListener::bind(addr).await.unwrap(), app)
        .await
        .unwrap();
}
