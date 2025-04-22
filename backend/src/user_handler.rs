use crate::db::connect_db;
use crate::models::*;
use crate::schema::users::dsl::*;
use axum::{Json, http::StatusCode};
use diesel::prelude::*;
use log::error;
use serde_json::json;

pub async fn hello_user_json() -> Result<Json<serde_json::Value>, (StatusCode, String)> {
    let body = json!({ "message": "hello User" });
    Ok(Json(body))
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
pub async fn create_user(
    Json(payload): Json<CreateUserRequest>,
) -> Result<StatusCode, (StatusCode, String)> {
    let mut conn = connect_db();

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
