use tower_http::cors::{Any, CorsLayer};

use axum::{
    Router,
    routing::{get, post},
};

use crate::handlers::user_handler::{create_user, generate_token, hello_user_json, validate_user};

pub fn create_router() -> Router {
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any).allow_headers(Any);

    Router::new()
        .route("/", get(hello_user_json))
        .route("/create", post(create_user))
        .route("/validate", post(validate_user))
        .route("/token", post(generate_token))
        .layer(cors)
}