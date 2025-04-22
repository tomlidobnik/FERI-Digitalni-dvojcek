use crate::auth::create_jwt;
use crate::auth::verify_jwt;
use crate::db::connect_db;
use crate::models::{LoginRequest, User};
use crate::schema::users::dsl::*;
use axum::{Json, http::StatusCode, response::IntoResponse};
use axum_extra::extract::TypedHeader;
use diesel::prelude::*;
use headers::{Authorization, authorization::Bearer};
use log::error;
use serde_json::json;

pub async fn hello_user_json(
    header: Option<TypedHeader<Authorization<Bearer>>>,
) -> impl IntoResponse {
    match header {
        Some(TypedHeader(Authorization(bearer))) => match verify_jwt(bearer.token()) {
            Ok(data) => {
                let other_username = data.claims.sub;
                let body = json!({ "message": format!("hello {}", other_username) });
                (StatusCode::OK, Json(body)).into_response()
            }
            Err(_) => (StatusCode::UNAUTHORIZED, "Invalid or expired token").into_response(),
        },
        None => (StatusCode::UNAUTHORIZED, "Authorization header missing").into_response(),
    }
}

pub async fn validate_user(
    Json(payload): Json<LoginRequest>,
) -> Result<(StatusCode, String), (StatusCode, String)> {
    let mut connection = connect_db();

    match users
        .filter(username.eq(&payload.username))
        .first::<User>(&mut connection)
    {
        Ok(user) => {
            if user.password == payload.password {
                Ok((StatusCode::OK, "Valid user".into()))
            } else {
                Err((StatusCode::UNAUTHORIZED, "Invalid password".into()))
            }
        }
        Err(e) => {
            error!("DB error: {:?}", e);
            Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))
        }
    }
}

pub async fn generate_token(Json(payload): Json<LoginRequest>) -> impl IntoResponse {
    match validate_user(Json(payload.clone())).await {
        Ok((StatusCode::OK, _)) => {
            let token = create_jwt(&payload.username);
            (StatusCode::OK, token)
        }
        Ok((status, msg)) => (status, msg),
        Err((status, msg)) => (status, msg),
    }
}

