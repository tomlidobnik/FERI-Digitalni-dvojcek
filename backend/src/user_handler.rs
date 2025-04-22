use axum::{http::StatusCode, response::Json};
use serde_json::json;

pub async fn hello_user_json() -> Result<Json<serde_json::Value>, (StatusCode, String)> {
    let body = json!({ "message": "hello User" });
    Ok(Json(body))
}
