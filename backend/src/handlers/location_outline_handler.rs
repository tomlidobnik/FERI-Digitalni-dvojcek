use crate::config::db;
use crate::error::location_outline_error::LocationOutlineError;
use crate::models::LocationOutline;
use crate::schema::locations::dsl::locations;
use crate::schema::location_outline::dsl::{location_outline, points, id as outline_id};
use axum::{extract::Path, http::StatusCode, Json};
use diesel::prelude::*;
use diesel::insert_into;
use log::info;
use serde::Deserialize;
use serde_json::Value;

#[derive(Deserialize)]
pub struct UpdateLocationOutlineRequest {
    pub id: i32,
    pub points: Value,
}

#[derive(Deserialize)]
pub struct CreateLocationOutlineRequest {
    pub info: Option<String>,
    pub points: Value,
}

#[derive(Insertable, Deserialize)]
#[diesel(table_name = crate::schema::location_outline)]
pub struct NewLocationOutline {
    pub points: Value,
}

#[derive(Insertable, Deserialize)]
#[diesel(table_name = crate::schema::locations)]
pub struct NewLocation {
    pub info: Option<String>,
    pub location_outline_fk: i32,
}

pub async fn create_location_outline(
    Json(payload): Json<CreateLocationOutlineRequest>,
) -> Result<StatusCode, LocationOutlineError> {
    info!("Called create_location_outline with info: {:?}, points: {:?}", payload.info, payload.points);
    let mut conn = db::connect_db();

    let new_outline = NewLocationOutline {
        points: payload.points,
    };

    let inserted_outline: LocationOutline = match diesel::insert_into(location_outline)
        .values(&new_outline)
        .get_result(&mut conn)
    {
        Ok(outline) => outline,
        Err(_) => return Err(LocationOutlineError::InternalServerError),
    };

    let new_location = NewLocation {
        info: payload.info,
        location_outline_fk: inserted_outline.id,
    };

    match insert_into(locations).values(&new_location).execute(&mut conn) {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn update_location_outline(
    Json(payload): Json<UpdateLocationOutlineRequest>,
) -> Result<StatusCode, LocationOutlineError> {
    info!("Called update_location_outline for id: {}, points: {:?}", payload.id, payload.points);
    let mut conn = db::connect_db();
    match diesel::update(location_outline.filter(outline_id.eq(payload.id)))
        .set(points.eq(payload.points))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn delete_location_outline(
    Path(id_value): Path<i32>,
) -> Result<StatusCode, LocationOutlineError> {
    info!("Called delete_location_outline for id: {}", id_value);
    let mut conn = db::connect_db();
    match diesel::delete(location_outline.filter(outline_id.eq(id_value))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(LocationOutlineError::NotFound),
        Err(_) => Err(LocationOutlineError::InternalServerError),
    }
}

pub async fn get_location_outline_by_id(
    Path(id_value): Path<i32>,
) -> Result<Json<LocationOutline>, LocationOutlineError> {
    info!("Called get_location_outline_by_id for id: {}", id_value);
    let mut conn = db::connect_db();
    match location_outline.filter(outline_id.eq(id_value)).first::<LocationOutline>(&mut conn) {
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