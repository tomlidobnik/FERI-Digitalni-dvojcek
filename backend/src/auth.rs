use chrono::{Duration, Utc};
use jsonwebtoken::{
    DecodingKey, EncodingKey, Header, TokenData, Validation, decode, encode, errors::Result,
};
use serde::{Deserialize, Serialize};
fn get_secret() -> &'static [u8] {
    dotenvy::dotenv().ok();
    let secret = std::env::var("JWT_SECRET").expect("JWT_SECRET must be set");
    Box::leak(secret.into_bytes().into_boxed_slice())
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Claims {
    pub sub: String,
    pub exp: usize,
}

pub fn create_jwt(user_id: &str) -> String {
    let expiration = Utc::now()
        .checked_add_signed(Duration::hours(24))
        .expect("valid timestamp")
        .timestamp() as usize;

    let claims = Claims {
        sub: user_id.to_owned(),
        exp: expiration,
    };

    encode(
        &Header::default(),
        &claims,
        &EncodingKey::from_secret(get_secret()),
    )
    .unwrap()
}

pub fn verify_jwt(token: &str) -> Result<TokenData<Claims>> {
    decode::<Claims>(
        token,
        &DecodingKey::from_secret(get_secret()),
        &Validation::default(),
    )
}
