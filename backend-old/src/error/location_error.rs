use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum LocationError {
    #[error("Lokacija ni bila najdena")]
    LocationNotFound,
    #[error("Napaka strežnika")]
    InternalServerError,
}

impl IntoResponse for LocationError {
    fn into_response(self) -> Response {
        let status_code = match self {
            LocationError::LocationNotFound => StatusCode::NOT_FOUND,
            LocationError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let body = Json(json!({ "error": self.to_string() }));
        (status_code, body).into_response()
    }
}