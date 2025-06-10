// @generated automatically by Diesel CLI.

diesel::table! {
    chat_messages (id) {
        id -> Int4,
        user_fk -> Int4,
        message -> Text,
        created_at -> Timestamp,
        event_fk -> Int4,
    }
}

diesel::table! {
    event_allowed_users (event_id, user_id) {
        event_id -> Int4,
        user_id -> Int4,
    }
}

diesel::table! {
    event_users (event_id, user_id) {
        event_id -> Int4,
        user_id -> Int4,
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
        public -> Bool,
        tag -> Nullable<Text>,
    }
}

diesel::table! {
    friend_chat_messages (id) {
        id -> Int4,
        user_fk -> Int4,
        message -> Text,
        created_at -> Timestamp,
        friend_fk -> Int4,
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

diesel::joinable!(chat_messages -> events (event_fk));
diesel::joinable!(chat_messages -> users (user_fk));
diesel::joinable!(event_allowed_users -> events (event_id));
diesel::joinable!(event_allowed_users -> users (user_id));
diesel::joinable!(event_users -> events (event_id));
diesel::joinable!(event_users -> users (user_id));
diesel::joinable!(events -> locations (location_fk));
diesel::joinable!(events -> users (user_fk));
diesel::joinable!(friend_chat_messages -> friends (friend_fk));
diesel::joinable!(friend_chat_messages -> users (user_fk));
diesel::joinable!(locations -> location_outline (location_outline_fk));

diesel::allow_tables_to_appear_in_same_query!(
    chat_messages,
    event_allowed_users,
    event_users,
    events,
    friend_chat_messages,
    friends,
    location_outline,
    locations,
    users,
);
