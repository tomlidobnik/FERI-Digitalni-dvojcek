use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum ChatError {
    #[error("Napaka strežnika")]
    InternalServerError,
}

impl IntoResponse for ChatError {
    fn into_response(self) -> Response {
        let status_code = match self {
            ChatError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let body = Json(json!({ "error": self.to_string() }));
        (status_code, body).into_response()
    }
}