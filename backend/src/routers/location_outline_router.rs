use axum::{Router, routing::{post, get, delete, put}};

use crate::handlers::location_outline_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/create", post(create_location_outline))
        .route("/update", put(update_location_outline))
        .route("/all", get(get_all_location_outlines))
        .route("/by_id/{id}", get(get_location_outline_by_id))
        .route("/delete/{id}", delete(delete_location_outline))
}