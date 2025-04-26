// @generated automatically by Diesel CLI.

diesel::table! {
    events (id) {
        id -> Int4,
        user_fk -> Nullable<Int4>,
        title -> Text,
        description -> Text,
    }
}

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

diesel::joinable!(events -> users (user_fk));

diesel::allow_tables_to_appear_in_same_query!(
    events,
    users,
);
