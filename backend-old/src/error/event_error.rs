use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum EventError {
    #[error("Dogodek ni bil najden")]
    EventNotFound,
    #[error("Ni dovoljenja za spremembo tega dogodka")]
    Unauthorized,
    #[error("Napaka strežnika")]
    InternalServerError,
}

impl IntoResponse for EventError {
    fn into_response(self) -> Response {
        let status_code = match self {
            EventError::EventNotFound => StatusCode::NOT_FOUND,
            EventError::Unauthorized => StatusCode::UNAUTHORIZED,
            EventError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let body = Json(json!({ "error": self.to_string() }));
        (status_code, body).into_response()
    }
}