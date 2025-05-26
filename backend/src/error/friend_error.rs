use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum FriendError {
    #[error("Prijateljstvo ni bilo najdeno")]
    FriendNotFound,
    #[error("Prijateljstvo že obstaja")]
    FriendAlreadyExists,
    #[error("Ni dovoljenja za spremembo prijateljstva")]
    Unauthorized,
    #[error("Napaka strežnika")]
    InternalServerError,
}

impl IntoResponse for FriendError {
    fn into_response(self) -> Response {
        let status_code = match self {
            FriendError::FriendNotFound => StatusCode::NOT_FOUND,
            FriendError::FriendAlreadyExists => StatusCode::BAD_REQUEST,
            FriendError::Unauthorized => StatusCode::UNAUTHORIZED,
            FriendError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let body = Json(json!({ "error": self.to_string() }));
        (status_code, body).into_response()
    }
}