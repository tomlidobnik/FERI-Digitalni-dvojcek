use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum LocationOutlineError {
    #[error("Location outline not found")]
    NotFound,
    #[error("Internal server error")]
    InternalServerError,
}

impl IntoResponse for LocationOutlineError {
    fn into_response(self) -> Response {
        let status_code = match self {
            LocationOutlineError::NotFound => StatusCode::NOT_FOUND,
            LocationOutlineError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let body = Json(json!({ "error": self.to_string() }));
        (status_code, body).into_response()
    }
}