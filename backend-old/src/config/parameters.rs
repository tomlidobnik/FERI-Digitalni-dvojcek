use dotenvy;

pub fn init() {
    dotenvy::dotenv().expect("Failed to load .env file");
}

pub fn get(parameter: &str) -> String {
    std::env::var(parameter)
        .unwrap_or_else(|_| panic!("{} is not defined in the environment.", parameter))
}

pub fn get_i64(parameter: &str) -> i64 {
    std::env::var(parameter)
        .unwrap_or_else(|_| panic!("{} is not defined in the environment.", parameter))
        .parse::<i64>()
        .unwrap_or_else(|_| panic!("{} is not a valid i64 value.", parameter))
}
