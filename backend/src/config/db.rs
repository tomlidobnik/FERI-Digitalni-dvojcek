use crate::config::parameters;
use diesel::prelude::*;
pub fn connect_db() -> PgConnection {
    let database_url = parameters::get("DATABASE_URL");
    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Error connecting to {}", database_url))
}
