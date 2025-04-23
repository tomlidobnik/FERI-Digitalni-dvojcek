use axum::{
    extract::FromRequestParts,
    http::{StatusCode, request::Parts},
};
use headers::{Authorization, HeaderMapExt};

use crate::config::parameters;
use crate::models::{AuthenticatedUser, Claims};
use chrono::{Duration, Utc};
use jsonwebtoken::{
    DecodingKey, EncodingKey, Header, TokenData, Validation, decode, encode,
    errors::Error as JwtError,
};
use once_cell::sync::OnceCell;

fn get_secret() -> &'static [u8] {
    static SECRET: OnceCell<Box<[u8]>> = OnceCell::new();
    SECRET.get_or_init(|| {
        parameters::get("JWT_SECRET")
            .into_bytes()
            .into_boxed_slice()
    })
}

impl<S> FromRequestParts<S> for AuthenticatedUser
where
    S: Send + Sync,
{
    type Rejection = (StatusCode, &'static str);

    async fn from_request_parts(parts: &mut Parts, _state: &S) -> Result<Self, Self::Rejection> {
        let token = parts
            .headers
            .typed_get::<Authorization<headers::authorization::Bearer>>()
            .map(|Authorization(bearer)| bearer.token().to_string());

        match token {
            Some(t) => match verify_jwt(&t) {
                Ok(data) => Ok(Self(data.claims)),
                Err(_) => Err((StatusCode::UNAUTHORIZED, "Invalid or expired token")),
            },
            None => Err((StatusCode::UNAUTHORIZED, "Missing authorization header")),
        }
    }
}

pub fn create_jwt(user_id: &str) -> String {
    let expiration = Utc::now()
        .checked_add_signed(Duration::minutes(parameters::get_i64("JWT_TTL_IN_MINUTES")))
        .expect("valid timestamp")
        .timestamp() as usize;

    let claims = Claims {
        sub: user_id.to_string(),
        exp: expiration,
    };

    encode(
        &Header::default(),
        &claims,
        &EncodingKey::from_secret(get_secret()),
    )
    .unwrap()
}

pub fn verify_jwt(token: &str) -> Result<TokenData<Claims>, JwtError> {
    decode::<Claims>(
        token,
        &DecodingKey::from_secret(get_secret()),
        &Validation::default(),
    )
}
