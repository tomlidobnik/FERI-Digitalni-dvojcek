use chrono::NaiveDateTime;
use diesel::prelude::*;
use serde::Deserialize;
use serde::Serialize;
use serde_json::Value;


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
#[diesel(table_name = crate::schema::friends)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct Friend {
    pub id: i32,
    pub user1_fk: Option<i32>,
    pub user2_fk: Option<i32>,
    pub status: i32,
}

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::schema::events)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct Event {
    pub id: i32,
    pub user_fk: Option<i32>,
    pub title: String,
    pub description: String,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub location_fk: Option<i32>,
}
#[derive(Deserialize)]
pub struct UpdateEventRequest {
    pub id: i32,
    pub title: String,
    pub description: String,
}

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::schema::location_outline)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct LocationOutline {
    pub id: i32,
    pub points: Value,
}

#[derive(Insertable, Deserialize)]
#[diesel(table_name = crate::schema::location_outline)]
pub struct NewLocationOutline {
    pub points: Value,
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
    pub longitude: Option<f32>,
    pub latitude: Option<f32>,
    pub location_outline_fk: Option<i32>,
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
#[derive(Debug, Clone, serde::Deserialize)]
pub struct FriendRequest {
    pub username: String,
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
#[derive(Serialize, Deserialize)]
pub struct PublicUserDataRequest {
    pub username: String,
}

#[derive(Serialize, Deserialize)]
pub struct UserResponse {
    pub username: String,
    pub first_name: String,
    pub last_name: String,
    pub email: String,
}

#[derive(Queryable, Selectable, Serialize)]
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
