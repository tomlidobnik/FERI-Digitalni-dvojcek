use axum::{Router, routing::get, routing::post};

use crate::handlers::friend_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/send", post(friend_request))
        .route("/list", get(list_friends))
}
