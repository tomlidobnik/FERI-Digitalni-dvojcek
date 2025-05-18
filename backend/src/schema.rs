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
        start_date -> Timestamp,
        end_date -> Timestamp,
        location_fk -> Nullable<Int4>,
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
    location_outline (id) {
        id -> Int4,
        points -> Jsonb,
    }
}

diesel::table! {
    locations (id) {
        id -> Int4,
        info -> Nullable<Text>,
        longitude -> Nullable<Float4>,
        latitude -> Nullable<Float4>,
        location_outline_fk -> Nullable<Int4>,
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

diesel::joinable!(events -> locations (location_fk));
diesel::joinable!(events -> users (user_fk));
diesel::joinable!(locations -> location_outline (location_outline_fk));

diesel::allow_tables_to_appear_in_same_query!(
    chat_messages,
    events,
    friends,
    location_outline,
    locations,
    users,
);
