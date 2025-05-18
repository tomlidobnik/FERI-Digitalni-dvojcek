use crate::config::db;
use crate::error::location_outline_error::LocationOutlineError;
use crate::models::LocationOutline;
use crate::schema::location_outline::dsl::*;
use axum::{extract::Path, http::StatusCode, Json};
use diesel::prelude::*;
use log::info;
use serde::Deserialize;
use serde_json::Value;

#[derive(Deserialize)]
pub struct UpdateLocationOutlineRequest {
    pub id: i32,
    pub points: Value,
}

#[derive(Insertable, Deserialize)]
#[diesel(table_name = crate::schema::location_outline)]
pub struct NewLocationOutline {
    pub points: Value,
}

pub async fn create_location_outline(
    Json(payload): Json<NewLocationOutline>,
) -> Result<StatusCode, LocationOutlineError> {
    info!("Called create_location_outline with points: {:?}", payload.points);
    let mut conn = db::connect_db();
    match diesel::insert_into(location_outline)
        .values(&payload)
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn update_location_outline(
    Json(payload): Json<UpdateLocationOutlineRequest>,
) -> Result<StatusCode, LocationOutlineError> {
    info!("Called update_location_outline for id: {}, points: {:?}", payload.id, payload.points);
    let mut conn = db::connect_db();
    match diesel::update(location_outline.filter(id.eq(payload.id)))
        .set(points.eq(payload.points))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn delete_location_outline(
    Path(outline_id): Path<i32>,
) -> Result<StatusCode, LocationOutlineError> {
    info!("Called delete_location_outline for id: {}", outline_id);
    let mut conn = db::connect_db();
    match diesel::delete(location_outline.filter(id.eq(outline_id))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(LocationOutlineError::NotFound),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn get_location_outline_by_id(
    Path(outline_id): Path<i32>,
) -> Result<Json<LocationOutline>, LocationOutlineError> {
    info!("Called get_location_outline_by_id for id: {}", outline_id);
    let mut conn = db::connect_db();
    match location_outline.filter(id.eq(outline_id)).first::<LocationOutline>(&mut conn) {
        Ok(outline) => Ok(Json(outline)),
        Err(diesel::result::Error::NotFound) => Err(LocationOutlineError::NotFound),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn get_all_location_outlines() -> Result<Json<Vec<LocationOutline>>, LocationOutlineError> {
    info!("Called get_all_location_outlines");
    let mut conn = db::connect_db();
    match location_outline.load::<LocationOutline>(&mut conn) {
        Ok(outlines) => Ok(Json(outlines)),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}