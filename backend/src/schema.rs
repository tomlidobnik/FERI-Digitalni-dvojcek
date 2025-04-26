// @generated automatically by Diesel CLI.

diesel::table! {
    users (id) {
        id -> Int4,
        username -> Text,
        firstname -> Text,
        lastname -> Text,
        email -> Text,
        password -> Text,
    }
}
