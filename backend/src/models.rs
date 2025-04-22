use diesel::prelude::*;
use serde::Deserialize;
use serde::Serialize;

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::schema::users)]
#[diesel(check_for_backend(diesel::pg::Pg))]
pub struct User {
    pub id: i32,
    pub username: String,
    pub password: String,
}
#[derive(Deserialize)]
pub struct LoginRequest {
    pub username: String,
    pub password: String,
}
#[derive(Insertable)]
#[diesel(table_name = crate::schema::users)]
pub struct NewUser<'a> {
    pub username: &'a str,
    pub password: &'a str,
}
#[derive(Deserialize)]
pub struct CreateUserRequest {
    pub username: String,
    pub password: String,
}
