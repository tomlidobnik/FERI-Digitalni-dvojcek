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
        .route("/add_user_to_event/{id}", post(add_user_to_private_event))
        .route("/my", get(get_user_events))
        .route("/join_public_event/{id}", post(join_public_event))
        .route("/leave_event/{id}", post(leave_event))
        .route("/remove_user_from_event/{event_id}/{user_id}", delete(remove_user_from_private_event))
        .route("/get_users/{event_id}/", get(get_user_at_events))
}