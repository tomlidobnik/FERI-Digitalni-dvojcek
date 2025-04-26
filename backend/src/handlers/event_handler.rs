use crate::config::db;
use crate::models::{AuthenticatedUser, CreateEventRequest, NewEvent, UpdateEventRequest};
use crate::schema::events::dsl::*;
use crate::schema::users::dsl::*;
use axum::{Json, http::StatusCode};
use diesel::prelude::*;
use log::info;

async fn get_user_id(user: AuthenticatedUser) -> Result<i32, StatusCode> {
    let mut conn = db::connect_db();
    let user_id: i32 = users
        .filter(username.eq(&user.0.sub))
        .select(crate::schema::users::id)
        .first(&mut conn)
        .map_err(|_| StatusCode::UNAUTHORIZED)?;

    Ok(user_id)
}

pub async fn update_event(
    user: AuthenticatedUser,
    Json(payload): Json<UpdateEventRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called update_event by user: {}", user.0.sub);
    let mut conn = db::connect_db();
    match diesel::update(events.filter(crate::schema::events::id.eq(&payload.id)))
        .set((
            title.eq(&payload.title),
            description.eq(&payload.description),
        ))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn create_event(
    user: AuthenticatedUser,
    Json(payload): Json<CreateEventRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called create_event by user: {}", user.0.sub);

    let mut conn = db::connect_db();

    let user_id: i32 = get_user_id(user).await?;
    let new_event = NewEvent {
        title: payload.title,
        description: payload.description,
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
