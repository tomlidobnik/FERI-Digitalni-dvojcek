pub mod models;
pub mod schema;

mod config;
mod error;
mod handlers;
mod middleware;
mod routers;


#[tokio::main]
async fn main() {
    config::parameters::init();
    routers::root::run_server().await;
}
