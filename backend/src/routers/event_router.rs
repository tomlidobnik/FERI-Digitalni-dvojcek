use axum::{Router, routing::post};

use crate::handlers::event_handler::create_event;

pub fn create_router() -> Router {
    Router::new().route("/create", post(create_event))
}
