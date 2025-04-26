use axum::{
    Json,
    http::StatusCode,
    response::{IntoResponse, Response},
};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum UserError {
    #[error("User not found")]
    UserNotFound,
    #[error("User already exists")]
    UserAlreadyExists,
    #[error("Invalid password")]
    InvalidPassword,
    #[error("Email already used")]
    EmailAlreadyExists,
}

impl IntoResponse for UserError {
    fn into_response(self) -> Response {
        let status_code = match self {
            UserError::UserNotFound => StatusCode::NOT_FOUND,
            UserError::UserAlreadyExists => StatusCode::BAD_REQUEST,
            UserError::InvalidPassword => StatusCode::BAD_REQUEST,
            UserError::EmailAlreadyExists => StatusCode::BAD_REQUEST,
        };

        let body = Json(json!({
            "error": self.to_string()
        }));

        (status_code, body).into_response()
    }
}
