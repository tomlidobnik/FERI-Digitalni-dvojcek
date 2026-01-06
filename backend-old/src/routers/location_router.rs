use axum::{Router, routing::{post, get, delete, put}};

use crate::handlers::location_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/create", post(create_location))
        .route("/update", put(update_location))
        .route("/all", get(get_all_locations))
        .route("/by_id/{:id}", get(get_location_by_id))
        .route("/delete/{:id}", delete(delete_location))
        .route("/list",get(get_all_locations_with_outline))
}