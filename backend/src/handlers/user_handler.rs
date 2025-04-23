use crate::config::db;
use crate::middleware::auth::create_jwt;
use crate::models::{AuthenticatedUser, CreateUserRequest, LoginRequest, NewUser, User};
use crate::schema::users::dsl::*;
use axum::{Json, http::StatusCode, response::IntoResponse};
use diesel::prelude::*;
use log::error;
use serde_json::json;

pub async fn hello_user_json(user: AuthenticatedUser) -> impl IntoResponse {
    let other_username = user.0.sub;
    let body = json!({ "message": format!("Hello, {}", other_username) });
    (StatusCode::OK, Json(body))
}

pub async fn validate_user(
    Json(payload): Json<LoginRequest>,
) -> Result<(StatusCode, String), (StatusCode, String)> {
    let mut connection = db::connect_db();

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
pub async fn create_user(
    Json(payload): Json<CreateUserRequest>,
) -> Result<StatusCode, (StatusCode, String)> {
    let mut conn = db::connect_db();

    match users
        .filter(username.eq(&payload.username))
        .first::<User>(&mut conn)
        .optional()
    {
        Ok(Some(_)) => Err((StatusCode::CONFLICT, "User already exists".into())),
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
                Err(e) => {
                    error!("Insert error: {:?}", e);
                    Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))
                }
            }
        }
        Err(e) => {
            error!("DB error: {:?}", e);
            Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))
        }
    }
}
