use crate::config::db;
use crate::error::user_error::UserError;
use crate::middleware::auth::create_jwt;
use crate::models::{
    AuthenticatedUser, User
};
use crate::schema::users::dsl::*;
use axum::{Json, extract::{Query, Path}, http::StatusCode, response::IntoResponse};
use bcrypt::{DEFAULT_COST, hash, verify};
use diesel::prelude::*;
use log::{error, info};
use serde::{Serialize,Deserialize};

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

#[derive(Deserialize)]
pub struct UpdateUserRequest {
    pub username: String,
    pub firstname: String,
    pub lastname: String,
    pub email: String,
    pub password: String,
}

async fn get_user_id(user: AuthenticatedUser) -> Result<i32, StatusCode> {
    let mut conn = db::connect_db();
    let user_id: i32 = users
        .filter(username.eq(&user.0.sub))
        .select(crate::schema::users::id)
        .first(&mut conn)
        .map_err(|_| StatusCode::UNAUTHORIZED)?;

    Ok(user_id)
}

pub async fn public_user_data(Query(params): Query<PublicUserDataRequest>) -> impl IntoResponse {
    info!("Called public_user_data for user {}", params.username);

    let mut connection = db::connect_db();
    let result = users
        .filter(username.eq(&params.username))
        .first::<User>(&mut connection);

    match result {
        Ok(user) => {
            let user_response = UserResponse {
                username: user.username,
                first_name: user.firstname,
                last_name: user.lastname,
                email: user.email,
            };
            Json(user_response).into_response()
        }
        Err(err) => {
            error!("Database error: {}", err);
            StatusCode::INTERNAL_SERVER_ERROR.into_response()
        }
    }
}

pub async fn private_user_data(user: AuthenticatedUser) -> impl IntoResponse {
    info!("Called private_user_data for user: {}", user.0.sub);
    let mut connection = db::connect_db();
    let result = users
        .filter(username.eq(&user.0.sub))
        .first::<User>(&mut connection);

    match result {
        Ok(user) => {
            let user_response = UserResponse {
                username: user.username,
                first_name: user.firstname,
                last_name: user.lastname,
                email: user.email,
            };
            Json(user_response).into_response()
        }
        Err(err) => {
            error!("Database error: {}", err);
            StatusCode::INTERNAL_SERVER_ERROR.into_response()
        }
    }
}

pub async fn create_user(Json(payload): Json<CreateUserRequest>) -> Result<StatusCode, UserError> {
    info!("Called create_user for username: {}", payload.username);
    let mut conn = db::connect_db();

    match users
        .filter(username.eq(&payload.username).or(email.eq(&payload.email)))
        .first::<User>(&mut conn)
        .optional()
    {
        Ok(Some(existing_user)) => {
            if existing_user.username == payload.username {
                Err(UserError::UserAlreadyExists)
            } else {
                Err(UserError::EmailAlreadyExists)
            }
        }
        Ok(None) => {
            let hashed_password = hash(payload.password, DEFAULT_COST)
                .map_err(|_| UserError::InternalServerError)?;
            let new_user = NewUser {
                username: payload.username,
                firstname: payload.firstname,
                lastname: payload.lastname,
                email: payload.email,
                password: hashed_password,
            };

            match diesel::insert_into(users)
                .values(&new_user)
                .execute(&mut conn)
            {
                Ok(_) => Ok(StatusCode::CREATED),
                Err(_) => Err(UserError::UserNotFound),
            }
        }
        Err(_) => Err(UserError::UserNotFound),
    }
}

pub async fn validate_user(
    Json(payload): Json<LoginRequest>,
) -> Result<(StatusCode, String), UserError> {
    info!("Called validate_user for username: {}", payload.username);
    let mut connection = db::connect_db();

    match users
        .filter(username.eq(&payload.username))
        .first::<User>(&mut connection)
    {
        Ok(user) => {
            match verify(payload.password, &user.password) {
                Ok(true) => Ok((StatusCode::OK, "Valid user".into())),
                Ok(false) => Err(UserError::InvalidPassword),
                Err(_) => Err(UserError::InternalServerError),
            }
        }
        Err(diesel::result::Error::NotFound) => Err(UserError::UserNotFound),
        Err(_) => Err(UserError::UserNotFound),
    }
}

pub async fn generate_token(
    Json(payload): Json<LoginRequest>,
) -> Result<(StatusCode, String), UserError> {
    info!("Called generate_token for username: {}", payload.username);
    match validate_user(Json(payload.clone())).await {
        Ok((StatusCode::OK, _)) => {
            let token = create_jwt(&payload.username);
            Ok((StatusCode::OK, token))
        }
        Ok((status, msg)) => Ok((status, msg)),
        Err(e) => Err(e),
    }
}

pub async fn get_all_users() -> Result<Json<Vec<User>>, StatusCode> {
    info!("Called get_all_users");
    let mut conn = db::connect_db();
    match users.load::<User>(&mut conn) {
        Ok(user_list) => Ok(Json(user_list)),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn get_user_by_id(
    Path(user_id): Path<i32>,
) -> Result<Json<User>, StatusCode> {
    info!("Called get_user_by_id for id: {}", user_id);
    let mut conn = db::connect_db();
    match users.filter(id.eq(user_id)).first::<User>(&mut conn) {
        Ok(user) => Ok(Json(user)),
        Err(diesel::result::Error::NotFound) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn delete_user(
    user: AuthenticatedUser
) -> Result<StatusCode, StatusCode> {
    info!("Called delete_user for username: {}", user.0.sub);
    let mut conn = db::connect_db();
    let user_id: i32 = get_user_id(user).await?;
    match diesel::delete(users.filter(id.eq(user_id))).execute(&mut conn) {
        Ok(affected) if affected > 0 => Ok(StatusCode::NO_CONTENT),
        Ok(_) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn update_user(
    user: AuthenticatedUser,
    Json(payload): Json<UpdateUserRequest>
) -> Result<StatusCode, UserError> {
    info!(
        "Called update_user for username: {} (id: {})",
        user.0.sub, payload.username
    );
    let mut conn = db::connect_db();

    let current_user = match users.filter(username.eq(&user.0.sub)).first::<User>(&mut conn) {
        Ok(user) => user,
        Err(diesel::result::Error::NotFound) => return Err(UserError::UserNotFound),
        Err(_) => return Err(UserError::UserNotFound),
    };

    let current_user_id: i32 = get_user_id(user).await.map_err(|_| UserError::Unauthorized)?;
    if payload.username != current_user.username {
        if users
            .filter(username.eq(&payload.username))
            .filter(id.ne(current_user_id))
            .first::<User>(&mut conn)
            .optional()
            .map_err(|_| UserError::InternalServerError)?
            .is_some()
        {
            return Err(UserError::UsernameTaken);
        }
    }

    if payload.email != current_user.email {
        if users
            .filter(email.eq(&payload.email))
            .filter(id.ne(current_user_id))
            .first::<User>(&mut conn)
            .optional()
            .map_err(|_| UserError::InternalServerError)?
            .is_some()
        {
            return Err(UserError::EmailTaken);
        }
    }

    match diesel::update(users.filter(id.eq(current_user_id)))
        .set((
            username.eq(&payload.username),
            firstname.eq(&payload.firstname),
            lastname.eq(&payload.lastname),
            email.eq(&payload.email),
            password.eq(&payload.password),
        ))
        .execute(&mut conn)
    {
        Ok(_) => Ok(StatusCode::OK),
        Err(_) => Err(UserError::UserNotFound),
    }
}
