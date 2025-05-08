use crate::{config::db, models::ChatMessage, schema::chat_messages::dsl::*};
use axum::{Json, http::StatusCode, response::IntoResponse};
use diesel::prelude::*;
use log::{error, info};

pub async fn get_chat_history() -> impl IntoResponse {
    info!("Called get_chat_history");
    let mut conn = db::connect_db();

    let result = chat_messages
        .order(created_at.asc())
        .load::<ChatMessage>(&mut conn);

    match result {
        Ok(messages) => Json(messages).into_response(),
        Err(err) => {
            error!("Database error: {}", err);
            StatusCode::INTERNAL_SERVER_ERROR.into_response()
        }
    }
}

