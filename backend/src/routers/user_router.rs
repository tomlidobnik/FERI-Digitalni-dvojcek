use crate::handlers::user_handler::*;
use crate::routers::friend_router;

use axum::{
    Router,
    routing::{get, post},
};

pub fn create_router() -> Router {
    Router::new()
        .route("/", get(hello_user_json))
        .route("/create", post(create_user))
        .route("/validate", post(validate_user))
        .route("/token", post(generate_token))
        .route("/public_data", get(public_user_data))
        .route("/private_data", get(private_user_data))
        .nest("/friends", friend_router::create_router())
}
