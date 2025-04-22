mod user_handler;
use axum::{Router, routing::get};
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};
use user_handler::hello_user_json;
mod db;

use db::connect_db;

pub mod models;
pub mod schema;
use log::{error, info, warn};

#[tokio::main]
async fn main() {
    pretty_env_logger::init();
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any);

    let app = Router::new()
        .route("/user", get(hello_user_json))
        .layer(cors);

    let addr: SocketAddr = "0.0.0.0:8000".parse().unwrap();
    info!("Server running at http://{}", addr);

    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}
