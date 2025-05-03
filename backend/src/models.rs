use bigdecimal::BigDecimal;
use diesel::prelude::*;
use serde::Deserialize;
use serde::Serialize;
use chrono::NaiveDateTime;

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::schema::users)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct User {
    pub id: i32,
    pub username: String,
    pub firstname: String,
    pub lastname: String,
    pub email: String,
    pub password: String,
}

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::schema::events)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct Event {
    pub id: i32,
    pub user_fk: Option<i32>,
    pub title: String,
    pub description: String,
}
#[derive(Deserialize)]
pub struct UpdateEventRequest {
    pub id: i32,
    pub title: String,
    pub description: String,
}
#[derive(Deserialize)]
pub struct CreateEventRequest {
    pub title: String,
    pub description: String,
}
#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::schema::locations)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct Location {
    pub id: i32,
    pub info: Option<String>,
    pub longitude: BigDecimal,
    pub latitude: BigDecimal,
}
#[derive(Deserialize)]
pub struct UpdateLocationRequest {
    pub id: i32,
    pub info: Option<String>,
    pub longitude: BigDecimal,
    pub latitude: BigDecimal,
}
#[derive(Deserialize)]
pub struct CreateLocationRequest {
    pub info: Option<String>,
    pub longitude: BigDecimal,
    pub latitude: BigDecimal,
}
#[derive(Insertable)]
#[diesel(table_name = crate::schema::locations)]
pub struct NewLocation {
    pub info: Option<String>,
    pub longitude: BigDecimal,
    pub latitude: BigDecimal,
}
#[derive(Insertable)]
#[diesel(table_name = crate::schema::events)]
pub struct NewEvent {
    pub user_fk: Option<i32>,
    pub title: String,
    pub description: String,
}

#[derive(Debug, Clone, serde::Deserialize)]
pub struct LoginRequest {
    pub username: String,
    pub password: String,
}

#[derive(Insertable)]
#[diesel(table_name = crate::schema::users)]
pub struct NewUser {
    pub username: String,
    pub firstname: String,
    pub lastname: String,
    pub email: String,
    pub password: String,
}
#[derive(Deserialize)]
pub struct CreateUserRequest {
    pub username: String,
    pub firstname: String,
    pub lastname: String,
    pub email: String,
    pub password: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Claims {
    pub sub: String,
    pub exp: usize,
}
pub struct AuthenticatedUser(pub Claims);

#[derive(Serialize, Deserialize)]
pub struct WsMessage {
    pub message: String,
    pub username: String,
}

#[derive(Queryable, Selectable,Serialize)]
#[diesel(table_name = crate::schema::chat_messages)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct ChatMessage {
    pub id: i32,
    pub username: String,
    pub message: String,
    pub created_at: NaiveDateTime,
}

#[derive(Insertable, Deserialize)]
#[diesel(table_name = crate::schema::chat_messages)]
pub struct NewChatMessage {
    pub username: String,
    pub message: String,
}