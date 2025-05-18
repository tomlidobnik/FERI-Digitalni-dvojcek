use crate::config::db;
use crate::error::event_error::EventError;
use crate::models::{AuthenticatedUser, Event};
use crate::schema::events::dsl::{events, title, description};
use crate::schema::events::{id as event_id_col, start_date, end_date, location_fk, public};
use crate::schema::users::dsl::*;
use crate::schema::event_allowed_users;
use axum::{Json, extract::Path, http::StatusCode};
use diesel::prelude::*;
use log::info;
use serde::Deserialize;
use chrono::NaiveDateTime;
use chrono::Utc;

#[derive(Insertable)]
#[diesel(table_name = crate::schema::events)]
pub struct NewEvent {
    pub user_fk: Option<i32>,
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
    pub public: bool,
}
#[derive(Deserialize)]
pub struct CreateEventRequest {
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
    pub public: bool,
}

#[derive(Deserialize)]
pub struct UpdateEventRequest {
    pub id: i32,
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
    pub public: bool,
}

#[derive(Queryable)]
pub struct EventAllowedUser {
    pub event_id: i32,
    pub user_id: i32,
}

#[derive(Insertable)]
#[diesel(table_name = crate::schema::event_allowed_users)]
pub struct NewEventAllowedUser {
    pub event_id: i32,
    pub user_id: i32,
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
) -> Result<StatusCode, EventError> {
    info!(
        "Called create_event by user: {} with title: {}, start_date: {}, end_date: {:?}, location_fk: {:?}",
        user.0.sub, payload.title, payload.start_date, payload.end_date, payload.location_fk
    );

    let mut conn = db::connect_db();

    let user_id: i32 = get_user_id(user).await.map_err(|_| EventError::Unauthorized)?;
    let new_event = NewEvent {
        title: payload.title,
        description: payload.description,
        start_date: payload.start_date,
        end_date: payload.end_date,
        location_fk: payload.location_fk,
        user_fk: Some(user_id),
        public: payload.public,
    };

    match diesel::insert_into(events)
        .values(&new_event)
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(EventError::InternalServerError),
    }
}

pub async fn get_event_by_id(
    user: AuthenticatedUser,
    Path(event_id): Path<i32>,
) -> Result<Json<Event>, EventError> {
    info!("Called get_event_by_id for id: {}", event_id);
    let mut conn = db::connect_db();
    let event = match events.filter(event_id_col.eq(event_id)).first::<Event>(&mut conn) {
        Ok(event) => event,
        Err(diesel::result::Error::NotFound) => return Err(EventError::EventNotFound),
        Err(_) => return Err(EventError::InternalServerError),
    };

    if !event.public {
        let user_id = get_user_id(user).await.map_err(|_| EventError::Unauthorized)?;
        let allowed = event_allowed_users::table
            .filter(event_allowed_users::event_id.eq(event_id))
            .filter(event_allowed_users::user_id.eq(user_id))
            .first::<EventAllowedUser>(&mut conn)
            .optional()
            .map_err(|_| EventError::InternalServerError)?;

        if allowed.is_none() {
            return Err(EventError::Unauthorized);
        }
    }

    Ok(Json(event))
}

pub async fn get_all_events() -> Result<Json<Vec<Event>>, EventError> {
    info!("Called get_all_events");
    let mut conn = db::connect_db();
    match events.load::<Event>(&mut conn) {
        Ok(event_list) => Ok(Json(event_list)),
        Err(_) => Err(EventError::InternalServerError),
    }
}

pub async fn delete_event(
    Path(event_id): Path<i32>,
) -> Result<StatusCode, EventError> {
    info!("Called delete_event for id: {}", event_id);
    let mut conn = db::connect_db();
    match diesel::delete(events.filter(event_id_col.eq(event_id))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(EventError::EventNotFound),
        Err(_) => Err(EventError::InternalServerError),
    }
}

pub async fn update_event(
    user: AuthenticatedUser,
    Json(payload): Json<UpdateEventRequest>,
) -> Result<StatusCode, EventError> {
    info!(
        "Called update_event by user: {} for event id: {} with title: {}",
        user.0.sub, payload.id, payload.title
    );
    let mut conn = db::connect_db();
    match diesel::update(events.filter(event_id_col.eq(&payload.id)))
        .set((
            title.eq(&payload.title),
            description.eq(&payload.description),
            start_date.eq(&payload.start_date),
            end_date.eq(&payload.end_date),
            location_fk.eq(&payload.location_fk),
            public.eq(&payload.public),
        ))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(EventError::InternalServerError),
    }
}

pub async fn get_available_events() -> Result<Json<Vec<Event>>, EventError> {
    use crate::schema::events::dsl::{events, end_date};
    let mut conn = db::connect_db();
    let now = Utc::now().naive_utc();

    let result = events
        .filter(end_date.gt(now))
        .load::<Event>(&mut conn);

    match result {
        Ok(event_list) => Ok(Json(event_list)),
        Err(_) => Err(EventError::InternalServerError),
    }
}