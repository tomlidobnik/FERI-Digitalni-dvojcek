use crate::config::db;
use crate::middleware::auth::create_jwt;
use crate::models::{AuthenticatedUser, CreateUserRequest, LoginRequest, NewUser, User};
use crate::schema::users::dsl::*;
use axum::{Json, http::StatusCode, response::IntoResponse};
use diesel::prelude::*;
use log::info;
use serde_json::json;
use crate::error::user_error::UserError;

pub async fn hello_user_json(user: AuthenticatedUser) -> impl IntoResponse {
    info!("Called hello_user_json");
    let other_username = user.0.sub;
    let body = json!({ "message": format!("Hello, {}", other_username) });
    (StatusCode::OK, Json(body))
}

pub async fn validate_user(
    Json(payload): Json<LoginRequest>,
) -> Result<(StatusCode, String), UserError> {
    info!("Called validate_user for username: {}", payload.username);
    let mut connection = db::connect_db();

    match users
        .filter(username.eq(&payload.username))
        .first::<User>(&mut connection)
    {
        Ok(user) => {
            if user.password == payload.password {
                Ok((StatusCode::OK, "Valid user".into()))
            } else {
                Err(UserError::InvalidPassword)
            }
        }
        Err(diesel::result::Error::NotFound) => Err(UserError::UserNotFound),
        Err(_) => Err(UserError::UserNotFound),
    }
}

pub async fn generate_token(Json(payload): Json<LoginRequest>) -> Result<(StatusCode, String), UserError> {
    info!("Called generate_token for username: {}", payload.username);
    match validate_user(Json(payload.clone())).await {
        Ok((StatusCode::OK, _)) => {
            let token = create_jwt(&payload.username);
            Ok((StatusCode::OK, token))
        }
        Ok((status, msg)) => Ok((status, msg)),
        Err(e) => Err(e),
    }
}

pub async fn create_user(
    Json(payload): Json<CreateUserRequest>,
) -> Result<StatusCode, UserError> {
    info!("Called create_user for username: {}", payload.username);
    let mut conn = db::connect_db();

    match users
        .filter(username.eq(&payload.username))
        .first::<User>(&mut conn)
        .optional()
    {
        Ok(Some(_)) => Err(UserError::UserAlreadyExists),
        Ok(None) => {
            let new_user = NewUser {
                username: &payload.username,
                password: &payload.password,
            };

            match diesel::insert_into(users)
                .values(&new_user)
                .execute(&mut conn)
            {
                Ok(_) => Ok(StatusCode::CREATED),
                Err(_) => Err(UserError::UserNotFound),
            }
        }
        Err(_) => Err(UserError::UserNotFound),
    }
}