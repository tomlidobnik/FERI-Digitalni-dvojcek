use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum LocationOutlineError {
    #[error("Location outline not found")]
    NotFound,
    #[error("Location outline already exists")]
    AlreadyExists,
    #[error("Unauthorized")]
    Unauthorized,
    #[error("Internal server error")]
    InternalServerError,
}

impl IntoResponse for LocationOutlineError {
    fn into_response(self) -> Response {
        let status_code = match self {
            LocationOutlineError::NotFound => StatusCode::NOT_FOUND,
            LocationOutlineError::AlreadyExists => StatusCode::BAD_REQUEST,
            LocationOutlineError::Unauthorized => StatusCode::UNAUTHORIZED,
            LocationOutlineError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let body = Json(json!({ "error": self.to_string() }));
        (status_code, body).into_response()
    }
}