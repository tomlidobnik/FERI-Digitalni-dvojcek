use crate::config::db;
use crate::error::friend_error::FriendError;
use crate::models::{AuthenticatedUser, Friend};
use crate::schema::friends::dsl::*;
use crate::schema::users::dsl::*;
use axum::{http::StatusCode, response::IntoResponse, Json};
use axum::extract::Path;
use diesel::prelude::*;

use log::info;

#[derive(Debug, Clone, serde::Deserialize)]
pub struct FriendRequest {
    pub username: String,
}

#[derive(serde::Serialize)]
pub struct FriendStatusResponse {
    pub status: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub friendship_id: Option<i32>,
}

async fn get_user_id(user: String) -> Result<i32, StatusCode> {
    let mut conn = db::connect_db();
    let user_id: i32 = users
        .filter(username.eq(user))
        .select(crate::schema::users::id)
        .first(&mut conn)
        .map_err(|_| StatusCode::UNAUTHORIZED)?;

    Ok(user_id)
}

pub async fn list_friends(user: AuthenticatedUser) -> Result<impl IntoResponse, FriendError> {
    info!("Called list_friends for user: {}", user.0.sub);
    let mut conn = db::connect_db();
    let this_user_id = match get_user_id(user.0.sub).await {
        Ok(this_id) => this_id,
        Err(_) => return Err(FriendError::Unauthorized),
    };

    let friend_rows = friends
        .filter(status.eq(0))
        .filter(user1_fk.eq(this_user_id).or(user2_fk.eq(this_user_id)))
        .load::<Friend>(&mut conn)
        .map_err(|_| FriendError::InternalServerError)?;

    let friend_ids: Vec<i32> = friend_rows
        .into_iter()
        .filter_map(|f| {
            if f.user1_fk == Some(this_user_id) {
                f.user2_fk
            } else {
                f.user1_fk
            }
        })
        .collect();

    let usernames_result = users
        .filter(crate::schema::users::id.eq_any(friend_ids))
        .select(crate::schema::users::username)
        .load::<String>(&mut conn);

    match usernames_result {
        Ok(names) => {
            info!("Found friends for user {}: {:?}", this_user_id, names);
            Ok(Json(names).into_response())
        },
        Err(_) => Err(FriendError::InternalServerError),
    }
}

pub async fn list_friends_ids(user: AuthenticatedUser) -> Result<impl IntoResponse, FriendError> {
    info!("Called list_friend_ids for user: {}", user.0.sub);
    let mut conn = db::connect_db();
    let this_user_id = match get_user_id(user.0.sub).await {
        Ok(this_id) => this_id,
        Err(_) => return Err(FriendError::Unauthorized),
    };

    let friend_rows = friends
        .filter(status.eq(0))
        .filter(user1_fk.eq(this_user_id).or(user2_fk.eq(this_user_id)))
        .load::<Friend>(&mut conn)
        .map_err(|_| FriendError::InternalServerError)?;

        let friendship_ids: Vec<i32> = friend_rows
        .into_iter()
        .map(|f| f.id)
        .collect();

    info!("Found friendship ids for user {}: {:?}", this_user_id, friendship_ids);
    Ok(Json(friendship_ids).into_response())
}

pub async fn friend_request(
    user: AuthenticatedUser,
    Json(payload): Json<FriendRequest>,
) -> Result<StatusCode, FriendError> {
    info!("Called friend_request for user: {} to {}", user.0.sub, payload.username);
    let mut conn = db::connect_db();
    let this_user_id = get_user_id(user.0.sub).await.map_err(|_| FriendError::Unauthorized)?;
    let friend_user_id = get_user_id(payload.username).await.map_err(|_| FriendError::FriendNotFound)?;

    let existing_friendship = friends
        .filter(
            (user1_fk.eq(this_user_id).and(user2_fk.eq(friend_user_id)))
                .or(user1_fk.eq(friend_user_id).and(user2_fk.eq(this_user_id))),
        )
        .first::<Friend>(&mut conn)
        .optional()
        .map_err(|_| FriendError::InternalServerError)?;

    if let Some(existing) = existing_friendship {
        if existing.status == 0 {
            info!("Friendship already exists and is active for users {} and {}", this_user_id, friend_user_id);
            return Err(FriendError::FriendAlreadyExists);
        } else if existing.status == this_user_id {
            info!("Friend request already sent by you (user {}) to user {}", this_user_id, friend_user_id);
            return Err(FriendError::FriendAlreadyExists);
        } else if existing.status == friend_user_id {
            info!("User {} is accepting friend request from user {}", this_user_id, friend_user_id);
            diesel::update(friends.find(existing.id))
                .set(status.eq(0))
                .execute(&mut conn)
                .map_err(|_| FriendError::InternalServerError)?;
            return Ok(StatusCode::OK);
        } else {
            return Err(FriendError::InternalServerError);
        }
    }

    let new_friend = (
        user1_fk.eq(this_user_id),
        user2_fk.eq(friend_user_id),
        status.eq(this_user_id),
    );

    info!("Creating new friendship between {} and {}", this_user_id, friend_user_id);
    diesel::insert_into(friends)
        .values(&new_friend)
        .execute(&mut conn)
        .map_err(|_| FriendError::InternalServerError)?;

    Ok(StatusCode::OK)
}

pub async fn friend_status(
    user: AuthenticatedUser,
    Path(friend_username): Path<String>,
) -> Result<impl IntoResponse, FriendError> {
    let mut conn = db::connect_db();
    let this_user_id = get_user_id(user.0.sub).await.map_err(|_| FriendError::Unauthorized)?;
    let friend_user_id = get_user_id(friend_username.clone()).await.map_err(|_| FriendError::FriendNotFound)?;

    let existing_friendship = friends
        .filter(
            (user1_fk.eq(this_user_id).and(user2_fk.eq(friend_user_id)))
                .or(user1_fk.eq(friend_user_id).and(user2_fk.eq(this_user_id))),
        )
        .first::<Friend>(&mut conn)
        .optional()
        .map_err(|_| FriendError::InternalServerError)?;

    let (status_str, f_id) = match existing_friendship {
        Some(f) if f.status == 0 => ("Friends", Some(f.id)),
        Some(f) if f.status == this_user_id => ("Request Sent", Some(f.id)),
        Some(f) if f.status == friend_user_id => ("Accept Friend Request", Some(f.id)),
        None => ("Not Friends", None),
        Some(f) => ("Request Pending", Some(f.id)),
    };

    Ok(Json(FriendStatusResponse {
        status: status_str.to_string(),
        friendship_id: f_id,
    }))
}

pub async fn remove_friend(
    user: AuthenticatedUser,
    Path(friend_username): Path<String>,
) -> Result<StatusCode, FriendError> {
    let mut conn = db::connect_db();
    let this_user_id = get_user_id(user.0.sub).await.map_err(|_| FriendError::Unauthorized)?;
    let friend_user_id = get_user_id(friend_username.clone()).await.map_err(|_| FriendError::FriendNotFound)?;

    let num_deleted = diesel::delete(
        friends.filter(
            (user1_fk.eq(this_user_id).and(user2_fk.eq(friend_user_id)))
                .or(user1_fk.eq(friend_user_id).and(user2_fk.eq(this_user_id))),
        )
    )
    .execute(&mut conn)
    .map_err(|_| FriendError::InternalServerError)?;

    if num_deleted > 0 {
        Ok(StatusCode::OK)
    } else {
        Err(FriendError::FriendNotFound)
    }
}
