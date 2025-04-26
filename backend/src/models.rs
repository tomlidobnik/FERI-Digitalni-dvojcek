use diesel::prelude::*;
use serde::Deserialize;
use serde::Serialize;

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
pub struct CreateEventRequest {
    pub title: String,
    pub description: String,
}
#[derive(Insertable)]
#[diesel(table_name = crate::schema::events)]
pub struct NewEvent<'a> {
    pub user_fk: Option<i32>,
    pub title: &'a str,
    pub description: &'a str,
}

#[derive(Debug, Clone, serde::Deserialize)]
pub struct LoginRequest {
    pub username: String,
    pub password: String,
}

#[derive(Insertable)]
#[diesel(table_name = crate::schema::users)]
pub struct NewUser<'a> {
    pub username: &'a str,
    pub firstname: &'a str,
    pub lastname: &'a str,
    pub email: &'a str,
    pub password: &'a str,
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
