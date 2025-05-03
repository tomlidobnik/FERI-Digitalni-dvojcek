use axum::{Json, http::StatusCode, response::IntoResponse};
use diesel::prelude::*;
use crate::{models::ChatMessage, schema::chat_messages::dsl::*, config::db};

pub async fn get_chat_history() -> impl IntoResponse {
    let mut conn = db::connect_db();

    let result = chat_messages
        .order(created_at.asc())
        .load::<ChatMessage>(&mut conn);

    match result {
        Ok(messages) => Json(messages).into_response(),
        Err(err) => {
            eprintln!("Database error: {}", err);
            StatusCode::INTERNAL_SERVER_ERROR.into_response()
        }
    }
}