use axum::{Json, Router, routing::get};
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};

pub mod models;
pub mod schema;

use diesel::prelude::*;
use dotenvy::dotenv;
use std::env;
use crate::models::Message;

pub fn establish_connection() -> PgConnection {
    dotenv().ok();
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    println!("DATABASE_URL: {:?}", database_url);
    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Error connecting to {}", database_url))
}

async fn get_message_handler() -> Result<Json<Message>, (axum::http::StatusCode, String)> {
    let mut connection = establish_connection();
    use crate::schema::messages::dsl::*;
    use diesel::dsl::sql;
    use diesel::sql_types::Double;

    match messages
        .order(sql::<Double>("RANDOM()"))
        .first::<Message>(&mut connection)
    {
        Ok(msg) => Ok(Json(msg)),
        Err(e) => {
            eprintln!("DB error: {:?}", e);
            Err((axum::http::StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))
        }
    }
}

#[tokio::main]
async fn main() {
    let cors = CorsLayer::new().allow_origin(Any).allow_methods(Any);

    let app = Router::new()
        .route("/", get(get_message_handler))
        .layer(cors);

    let addr: SocketAddr = "0.0.0.0:8000".parse().unwrap();
    println!("Server running at http://{}", addr);

    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}