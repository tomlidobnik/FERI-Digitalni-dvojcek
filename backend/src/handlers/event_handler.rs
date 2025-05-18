use crate::config::db;
use crate::models::{AuthenticatedUser, Event};
use crate::schema::events::dsl::{events, title, description};
use crate::schema::events::{id as event_id_col, start_date, end_date, location_fk};
use crate::schema::users::dsl::*;
use axum::{Json, extract::Path, http::StatusCode};
use diesel::prelude::*;
use log::info;
use serde::Deserialize;
use chrono::NaiveDateTime;

#[derive(Insertable)]
#[diesel(table_name = crate::schema::events)]
pub struct NewEvent {
    pub user_fk: Option<i32>,
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
}
#[derive(Deserialize)]
pub struct CreateEventRequest {
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
}

#[derive(Deserialize)]
pub struct UpdateEventRequest {
    pub id: i32,
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
}

async fn get_user_id(user: AuthenticatedUser) -> Result<i32, StatusCode> {
    let mut conn = db::connect_db();
    let user_id: i32 = users
        .filter(username.eq(&user.0.sub))
        .select(crate::schema::users::id)
        .first(&mut conn)
        .map_err(|_| StatusCode::UNAUTHORIZED)?;

    Ok(user_id)
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
        start_date: payload.start_date,
        end_date: payload.end_date,
        location_fk: payload.location_fk,
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

pub async fn get_event_by_id(
    Path(event_id): Path<i32>,
) -> Result<Json<Event>, StatusCode> {
    let mut conn = db::connect_db();
    match events.filter(event_id_col.eq(event_id)).first::<Event>(&mut conn) {
        Ok(event) => Ok(Json(event)),
        Err(diesel::result::Error::NotFound) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn get_all_events() -> Result<Json<Vec<Event>>, StatusCode> {
    let mut conn = db::connect_db();
    match events.load::<Event>(&mut conn) {
        Ok(event_list) => Ok(Json(event_list)),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn delete_event(
    Path(event_id): Path<i32>,
) -> Result<StatusCode, StatusCode> {
    let mut conn = db::connect_db();
    match diesel::delete(events.filter(event_id_col.eq(event_id))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn update_event(
    user: AuthenticatedUser,
    Json(payload): Json<UpdateEventRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called update_event by user: {}", user.0.sub);
    let mut conn = db::connect_db();
    match diesel::update(events.filter(event_id_col.eq(&payload.id)))
        .set((
            title.eq(&payload.title),
            description.eq(&payload.description),
            start_date.eq(&payload.start_date),
            end_date.eq(&payload.end_date),
            location_fk.eq(&payload.location_fk),
        ))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}