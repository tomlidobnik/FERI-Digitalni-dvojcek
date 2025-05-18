use crate::config::db;
use crate::schema::chat_messages::dsl::chat_messages;
use crate::schema::users::dsl::{users, username as username_col, id as user_id_col};
use axum::{
    extract::{
        ConnectInfo,
        ws::{Message, WebSocket, WebSocketUpgrade},
        Path,
    },
    response::IntoResponse,
};
use diesel::prelude::*;
use futures::{SinkExt, StreamExt};
use log::{error, info};
use std::net::SocketAddr;
use std::{collections::HashMap, sync::Arc};
use tokio::sync::{Mutex, mpsc};
use serde::{Serialize,Deserialize};

#[derive(Insertable, Deserialize)]
#[diesel(table_name = crate::schema::chat_messages)]
pub struct NewChatMessage {
    pub user_fk: i32,
    pub message: String,
    pub event_fk: i32,
}

#[derive(Serialize, Deserialize)]
pub struct WsMessage {
    pub message: String,
    pub username: String,
}

pub type Tx = mpsc::UnboundedSender<Message>;
pub type Connections = Arc<Mutex<HashMap<i32, HashMap<SocketAddr, Tx>>>>;

pub async fn handle_ws(
    Path(event_id): Path<i32>,
    ws: WebSocketUpgrade,
    ConnectInfo(addr): ConnectInfo<SocketAddr>,
    connections: Connections,
) -> impl IntoResponse {
    info!("WebSocket connection from {} to event {}", addr, event_id);
    ws.on_upgrade(move |socket| handle_socket(socket, addr, event_id, connections))
}

async fn handle_socket(
    socket: WebSocket,
    addr: SocketAddr,
    event_id: i32,
    connections: Connections,
) {
    let (mut sender, mut receiver) = socket.split();
    let (tx, mut rx) = mpsc::unbounded_channel::<Message>();

    {
        let mut all_conns = connections.lock().await;
        let room = all_conns.entry(event_id).or_insert_with(HashMap::new);
        room.insert(addr, tx);
    }

    let send_task = tokio::spawn(async move {
        while let Some(msg) = rx.recv().await {
            if sender.send(msg).await.is_err() {
                error!("Error sending message to WebSocket, closing connection");
                break;
            }
        }
    });

    let connections_clone = connections.clone();

    let recv_task = tokio::spawn(async move {
        while let Some(Ok(msg)) = receiver.next().await {
            if let Message::Text(text) = msg {
                if let Ok(parsed) = serde_json::from_str::<WsMessage>(&text) {
                    info!("Got message from {}: {}", parsed.username, parsed.message);

                    let username = parsed.username.clone();
                    let message = parsed.message.clone();
                    let event_id = event_id;

                    tokio::task::spawn_blocking(move || {
                        let mut conn = db::connect_db();
                        let user_fk = users
                            .filter(username_col.eq(&username))
                            .select(user_id_col)
                            .first::<i32>(&mut conn)
                            .ok();

                        if let Some(user_fk) = user_fk {
                            let new_msg = NewChatMessage {
                                user_fk,
                                message,
                                event_fk: event_id,
                            };
                            if let Err(e) = diesel::insert_into(chat_messages)
                                .values(&new_msg)
                                .execute(&mut conn)
                            {
                                error!("Failed to insert chat message: {}", e);
                            }
                        } else {
                            error!("User '{}' not found in database, message not saved.", username);
                        }
                    })
                    .await
                    .ok();

                    let json = serde_json::to_string(&parsed).unwrap();
                    broadcast_message(
                        &Message::Text(json.into()),
                        &connections_clone,
                        event_id,
                        addr,
                    )
                    .await;
                } else {
                    error!("Invalid JSON format received: {}", text);
                }
            }
        }
    });

    tokio::select! {
        _ = send_task => {},
        _ = recv_task => {},
    }

    {
        let mut all_conns = connections.lock().await;
        if let Some(room) = all_conns.get_mut(&event_id) {
            room.remove(&addr);
            if room.is_empty() {
                all_conns.remove(&event_id);
            }
        }
    }
}

async fn broadcast_message(
    message: &Message,
    connections: &Connections,
    event_id: i32,
    sender_addr: SocketAddr,
) {
    let all_conns = connections.lock().await;
    if let Some(room) = all_conns.get(&event_id) {
        for (addr, tx) in room.iter() {
            if addr != &sender_addr {
                let _ = tx.send(message.clone());
            }
        }
    }
}
