use crate::config::db;
use crate::models::LocationOutline;
use crate::schema::location_outline::dsl::*;
use axum::{Json, extract::Path, http::StatusCode};
use diesel::prelude::*;
use serde::Deserialize;
use serde_json::Value;
use log::info;

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
) -> Result<StatusCode, StatusCode> {
    info!("Called create_location_outline");
    let mut conn = db::connect_db();
    match diesel::insert_into(location_outline)
        .values(&payload)
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::CREATED),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn update_location_outline(
    Json(payload): Json<UpdateLocationOutlineRequest>,
) -> Result<StatusCode, StatusCode> {
    info!("Called update_location_outline");
    let mut conn = db::connect_db();
    match diesel::update(location_outline.filter(id.eq(payload.id)))
        .set(points.eq(payload.points))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn delete_location_outline(
    Path(outline_id): Path<i32>,
) -> Result<StatusCode, StatusCode> {
    let mut conn = db::connect_db();
    match diesel::delete(location_outline.filter(id.eq(outline_id))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn get_location_outline_by_id(
    Path(outline_id): Path<i32>,
) -> Result<Json<LocationOutline>, StatusCode> {
    let mut conn = db::connect_db();
    match location_outline.filter(id.eq(outline_id)).first::<LocationOutline>(&mut conn) {
        Ok(outline) => Ok(Json(outline)),
        Err(diesel::result::Error::NotFound) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn get_all_location_outlines() -> Result<Json<Vec<LocationOutline>>, StatusCode> {
    let mut conn = db::connect_db();
    match location_outline.load::<LocationOutline>(&mut conn) {
        Ok(outlines) => Ok(Json(outlines)),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}