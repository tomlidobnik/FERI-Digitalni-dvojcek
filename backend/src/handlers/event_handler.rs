use crate::config::db;
use crate::models::{AuthenticatedUser, CreateEventRequest, NewEvent};
use crate::schema::events::dsl::*;
use crate::schema::users::dsl::*;
use axum::{Json, http::StatusCode};
use diesel::prelude::*;
use log::info;

pub async fn create_event(
    user: AuthenticatedUser,
    Json(payload): Json<CreateEventRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called create_event by user: {}", user.0.sub);

    let mut conn = db::connect_db();
    let jwt_username = &user.0.sub;

    let user_id: i32 = users
        .filter(username.eq(jwt_username))
        .select(crate::schema::users::id)
        .first(&mut conn)
        .map_err(|_| StatusCode::UNAUTHORIZED)?;

    let new_event = NewEvent {
        title: &payload.title,
        description: &payload.description,
        user_fk: Some(user_id),
    };

    match diesel::insert_into(events)
        .values(&new_event)
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}
