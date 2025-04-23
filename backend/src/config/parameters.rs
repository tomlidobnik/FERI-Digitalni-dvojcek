use dotenvy;

pub fn init() {
    dotenvy::dotenv().ok().expect("Failed to load .env file");
}

pub fn get(parameter: &str) -> String {
    let env_parameter = std::env::var(parameter)
        .expect(&format!("{} is not defined in the environment.", parameter));
    return env_parameter;
}

pub fn get_i64(parameter: &str) -> i64 {
    let env_parameter = std::env::var(parameter)
        .expect(&format!("{} is not defined in the environment.", parameter));

    env_parameter
        .parse::<i64>()
        .expect(&format!("{} is not a valid i64 value.", parameter))
}
