mod config;
mod handlers;
mod middleware;
pub mod models;
mod routers;
pub mod schema;

use routers::root;
#[tokio::main]
async fn main() {
    config::parameters::init();
    root::run_server().await;
}
