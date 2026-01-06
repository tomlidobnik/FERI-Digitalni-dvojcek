use axum::{Router, routing::get};

use crate::handlers::chat_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/history/{event_id}", get(get_chat_history))
        .route("/friend_history/{friend_id}", get(get_friend_chat_history))
}
