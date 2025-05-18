use crate::{config::db, models::ChatMessage, schema::chat_messages::dsl::*};
use crate::error::chat_error::ChatError;
use axum::{Json, response::IntoResponse};
use diesel::prelude::*;
use log::{error, info};

pub async fn get_chat_history() -> Result<impl IntoResponse, ChatError> {
    info!("Called get_chat_history");
    let mut conn = db::connect_db();

    let result = chat_messages
        .order(created_at.asc())
        .load::<ChatMessage>(&mut conn);

    match result {
        Ok(messages) => Ok(Json(messages).into_response()),
        Err(err) => {
            error!("Database error: {}", err);
            Err(ChatError::InternalServerError)
        }
    }
}

