use crate::config::db;
use crate::schema::locations::dsl::*;
use diesel::prelude::*;
use log::info;
use serde::Deserialize;
use axum::{Json, extract::Path, http::StatusCode};
use crate::models::Location;
use crate::error::location_error::LocationError;

#[derive(Deserialize)]
pub struct UpdateLocationRequest {
    pub id: i32,
    pub info: Option<String>,
    pub longitude: Option<f32>,
    pub latitude: Option<f32>,
    pub location_outline_fk: Option<i32>,
}

#[derive(Deserialize)]
pub struct CreateLocationRequest {
    pub info: Option<String>,
    pub longitude: Option<f32>,
    pub latitude: Option<f32>,
    pub location_outline_fk: Option<i32>,
}

#[derive(Insertable)]
#[diesel(table_name = crate::schema::locations)]
pub struct NewLocation {
    pub info: Option<String>,
    pub longitude: Option<f32>,
    pub latitude: Option<f32>,
    pub location_outline_fk: Option<i32>,
}

pub async fn update_location(
    Json(payload): Json<UpdateLocationRequest>,
) -> Result<StatusCode, LocationError> {
    info!("Called update_location for id: {:?}", payload.id);
    let mut conn = db::connect_db();
    match diesel::update(locations.filter(id.eq(payload.id)))
        .set((
            info.eq(&payload.info),
            latitude.eq(&payload.latitude),
            longitude.eq(&payload.longitude),
            location_outline_fk.eq(&payload.location_outline_fk),
        ))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(LocationError::InternalServerError),
    }
}

pub async fn create_location(
    Json(payload): Json<CreateLocationRequest>,
) -> Result<StatusCode, LocationError> {
    info!(
        "Called create_location with info: {:?}, longitude: {:?}, latitude: {:?}, outline_fk: {:?}",
        payload.info, payload.longitude, payload.latitude, payload.location_outline_fk
    );
    let mut conn = db::connect_db();

    let new_location = NewLocation {
        info: payload.info,
        longitude: payload.longitude,
        latitude: payload.latitude,
        location_outline_fk: payload.location_outline_fk,
    };

    match diesel::insert_into(locations)
        .values(&new_location)
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(LocationError::InternalServerError),
    }
}

pub async fn get_location_by_id(
    Path(location_id): Path<i32>,
) -> Result<Json<Location>, LocationError> {
    info!("Called get_location_by_id for id: {}", location_id);
    let mut conn = db::connect_db();
    match locations.filter(id.eq(location_id)).first::<Location>(&mut conn) {
        Ok(location) => Ok(Json(location)),
        Err(diesel::result::Error::NotFound) => Err(LocationError::LocationNotFound),
        Err(_) => Err(LocationError::InternalServerError),
    }
}

pub async fn get_all_locations() -> Result<Json<Vec<Location>>, LocationError> {
    info!("Called get_all_locations");
    let mut conn = db::connect_db();
    match locations.load::<Location>(&mut conn) {
        Ok(location_list) => Ok(Json(location_list)),
        Err(_) => Err(LocationError::InternalServerError),
    }
}

pub async fn delete_location(
    Path(location_id): Path<i32>,
) -> Result<StatusCode, LocationError> {
    info!("Called delete_location for id: {}", location_id);
    let mut conn = db::connect_db();
    match diesel::delete(locations.filter(id.eq(location_id))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(LocationError::LocationNotFound),
        Err(_) => Err(LocationError::InternalServerError),
    }
}