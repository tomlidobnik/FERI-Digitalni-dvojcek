use crate::{config::db, schema::chat_messages::dsl::*};
use crate::error::chat_error::ChatError;
use axum::{Json, response::IntoResponse, extract::Path};
use diesel::prelude::*;
use log::{error, info};
use crate::schema::users::dsl::{users, username as users_username};
use crate::schema::users::id as user_id;
use serde::Serialize;
use chrono::NaiveDateTime;

#[derive(Serialize)]
pub struct ChatMessageResponse {
    pub username: String,
    pub message: String,
    pub date: NaiveDateTime,
}

pub async fn get_chat_history(
    Path(event_id): Path<i32>,
) -> Result<impl IntoResponse, ChatError> {
    info!("Called get_chat_history for event_id: {}", event_id);
    let mut conn = db::connect_db();

    let result = chat_messages
        .inner_join(users.on(user_id.eq(user_fk)))
        .filter(event_fk.eq(event_id))
        .order(created_at.asc())
        .select((users_username, message, created_at))
        .load::<(String, String, NaiveDateTime)>(&mut conn);

    match result {
        Ok(rows) => {
            let messages: Vec<ChatMessageResponse> = rows
                .into_iter()
                .map(|(username, other_message, date)| ChatMessageResponse { username, message: other_message, date })
                .collect();
            Ok(Json(messages).into_response())
        }
        Err(err) => {
            error!("Database error: {}", err);
            Err(ChatError::InternalServerError)
        }
    }
}

