use axum::{Router, routing::get};

use crate::handlers::chat_handler::*;

pub fn create_router() -> Router {
    Router::new()
        .route("/history/{evnet_id}", get(get_chat_history))
}
