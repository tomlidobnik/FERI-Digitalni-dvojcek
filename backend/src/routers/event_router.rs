use axum::{Router, routing::{post, get, delete, put}};

use crate::handlers::event_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/create", post(create_event))
        .route("/update", put(update_event))
        .route("/all", get(get_all_events))
        .route("/by_id/{id}", get(get_event_by_id))
        .route("/delete/{id}", delete(delete_event))
        .route("/available", get(get_available_events))
        .route("/make_public/{id}", put(make_event_public))
        .route("/make_private/{id}", put(make_event_private))
        .route("/allow_user/{id}", post(add_event_allowed_user))
        .route("/my", get(get_user_events))
}