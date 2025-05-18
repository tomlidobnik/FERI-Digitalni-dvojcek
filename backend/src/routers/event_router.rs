use axum::{Router, routing::{post, get, delete, put}};

use crate::handlers::event_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/create", post(create_event))
        .route("/update", put(update_event))
        .route("/all", get(get_all_events))
        .route("/by_id/{id}", get(get_event_by_id))
        .route("/delete/{id}", delete(delete_event))
}