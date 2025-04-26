use axum::{Router, routing::post};

use crate::handlers::event_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/create", post(create_event))
        .route("/update", post(update_event))
}
