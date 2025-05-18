use crate::handlers::user_handler::*;
use crate::routers::friend_router;

use axum::{
    Router,
    routing::{get, post, put, delete},
};

pub fn create_router() -> Router {
    Router::new()
        .route("/create", post(create_user))
        .route("/update", put(update_user))
        .route("/all", get(get_all_users))
        .route("/by_id/{id}", get(get_user_by_id))
        .route("/delete", delete(delete_user))
        .route("/validate", post(validate_user))
        .route("/token", post(generate_token))
        .route("/public_data", get(public_user_data))
        .route("/private_data", get(private_user_data))
        .nest("/friends", friend_router::create_router())
}