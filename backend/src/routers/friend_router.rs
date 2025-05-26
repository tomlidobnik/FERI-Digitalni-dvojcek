use axum::{Router, routing::{get,post,delete}};

use crate::handlers::friend_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/request", post(friend_request))
        .route("/status/{username}", get(friend_status))
        .route("/list", get(list_friends))
        .route("/list_ids", get(list_friends_ids))
        .route("/remove/{id}", delete(remove_friend))
}
