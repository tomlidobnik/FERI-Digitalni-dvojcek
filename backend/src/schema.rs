// @generated automatically by Diesel CLI.

diesel::table! {
    chat_messages (id) {
        id -> Int4,
        username -> Text,
        message -> Text,
        created_at -> Timestamp,
    }
}

diesel::table! {
    events (id) {
        id -> Int4,
        user_fk -> Nullable<Int4>,
        title -> Text,
        description -> Text,
    }
}

diesel::table! {
    friends (id) {
        id -> Int4,
        user1_fk -> Nullable<Int4>,
        user2_fk -> Nullable<Int4>,
        status -> Int4,
    }
}

diesel::table! {
    locations (id) {
        id -> Int4,
        info -> Nullable<Text>,
        longitude -> Numeric,
        latitude -> Numeric,
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

diesel::allow_tables_to_appear_in_same_query!(chat_messages, events, friends, locations, users,);
