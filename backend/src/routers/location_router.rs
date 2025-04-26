use axum::{Router, routing::post};

use crate::handlers::location_handler::*;

pub fn create_router() -> Router {
    Router::new().route("/create", post(create_location))
}
