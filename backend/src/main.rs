mod user_handler;
use axum::{
    Router,
    routing::{get, post},
};
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};
use user_handler::*;
mod auth;
mod db;
pub mod models;
pub mod schema;
use log::info;

#[tokio::main]
async fn main() {
    pretty_env_logger::init();
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any);

    let app = Router::new()
        .route("/user", get(hello_user_json))
        .route("/user/create", post(create_user))
        .route("/user/validate", post(validate_user))
        .route("/user/token", post(generate_token))
        .layer(cors);

    let addr: SocketAddr = "0.0.0.0:8000".parse().unwrap();
    info!("Server running at http://{}", addr);

    axum::serve(tokio::net::TcpListener::bind(addr).await.unwrap(), app)
        .await
        .unwrap();
}
