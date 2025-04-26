use crate::config::db;
use crate::models::{CreateLocationRequest, NewLocation, UpdateLocationRequest};
use crate::schema::locations::dsl::*;
use axum::{Json, http::StatusCode};
use diesel::prelude::*;
use log::info;

pub async fn update_location(
    Json(payload): Json<UpdateLocationRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called update_event");
    let mut conn = db::connect_db();
    match diesel::update(locations.filter(id.eq(&payload.id)))
        .set((
            info.eq(&payload.info),
            latitude.eq(&payload.latitude),
            longitude.eq(&payload.longitude),
        ))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn create_location(
    Json(payload): Json<CreateLocationRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called create_location");

    let mut conn = db::connect_db();

    let new_location = NewLocation {
        info: payload.info,
        longitude: payload.longitude,
        latitude: payload.latitude,
    };

    match diesel::insert_into(locations)
        .values(&new_location)
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}
