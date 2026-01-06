use axum::{
    Json,
    http::StatusCode,
    response::{IntoResponse, Response},
};
use serde_json::json;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum UserError {
    #[error("Uporabnik ni bil najden")]
    UserNotFound,
    #[error("Uporabnik že obstaja")]
    UserAlreadyExists,
    #[error("Neveljavno geslo")]
    InvalidPassword,
    #[error("E-naslov že obstaja")]
    EmailAlreadyExists,
    #[error("Uporabniško ime je že zasedeno")]
    UsernameTaken,
    #[error("E-naslov je že zaseden")]
    EmailTaken,
    #[error("Ni dovoljenja za spremembo tega uporabnika")]
    Unauthorized,
    #[error("Napaka strežnika")]
    InternalServerError,
}

impl IntoResponse for UserError {
    fn into_response(self) -> Response {
        let status_code = match self {
            UserError::UserNotFound => StatusCode::NOT_FOUND,
            UserError::UserAlreadyExists => StatusCode::BAD_REQUEST,
            UserError::InvalidPassword => StatusCode::BAD_REQUEST,
            UserError::EmailAlreadyExists => StatusCode::BAD_REQUEST,
            UserError::UsernameTaken => StatusCode::BAD_REQUEST,
            UserError::EmailTaken => StatusCode::BAD_REQUEST,
            UserError::Unauthorized => StatusCode::UNAUTHORIZED,
            UserError::InternalServerError => StatusCode::INTERNAL_SERVER_ERROR,
        };

        let body = Json(json!({
            "error": self.to_string()
        }));

        (status_code, body).into_response()
    }
}
