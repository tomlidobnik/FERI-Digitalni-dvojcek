use crate::models::{WsMessage, NewChatMessage};
use crate::config::db;
use crate::schema::chat_messages::dsl::chat_messages;
use axum::{
    extract::{
        ConnectInfo,
        ws::{Message, WebSocket, WebSocketUpgrade},
    },
    response::IntoResponse,
};
use diesel::prelude::*;
use std::net::SocketAddr;
use std::{collections::HashMap, sync::Arc};
use tokio::sync::{Mutex, mpsc};
use futures::{SinkExt, StreamExt};


pub type Tx = mpsc::UnboundedSender<Message>;
pub type Connections = Arc<Mutex<HashMap<SocketAddr, Tx>>>;

pub async fn handle_ws(
    ws: WebSocketUpgrade,
    ConnectInfo(addr): ConnectInfo<SocketAddr>,
    connections: Connections,
) -> impl IntoResponse {
    println!("WebSocket connection from {}", addr);
    ws.on_upgrade(move |socket| handle_socket(socket, addr, connections))
}

async fn handle_socket(
    socket: WebSocket,
    addr: SocketAddr,
    connections: Connections,
) {
    let (mut sender, mut receiver) = socket.split();
    let (tx, mut rx) = mpsc::unbounded_channel::<Message>();

    {
        let mut conn = connections.lock().await;
        conn.insert(addr, tx);
    }
    let send_task = tokio::spawn(async move {
        while let Some(msg) = rx.recv().await {
            if sender.send(msg).await.is_err() {
                eprintln!("Error sending message to WebSocket, closing connection");
                break;
            }
        }
    });
    let connections_clone = connections.clone();

    let recv_task = tokio::spawn(async move {
        while let Some(Ok(msg)) = receiver.next().await {
            if let Message::Text(text) = msg {
                if let Ok(parsed) = serde_json::from_str::<WsMessage>(&text) {
                    println!("Got message from {}: {}", parsed.username, parsed.message);

                    let new_msg = NewChatMessage {
                        username: parsed.username.clone(),
                        message: parsed.message.clone(),
                    };
                    tokio::task::spawn_blocking(move || {
                        let mut conn = db::connect_db();
                        diesel::insert_into(chat_messages)
                            .values(&new_msg)
                            .execute(&mut conn)
                            .ok();
                    }).await.ok();

                    let json = serde_json::to_string(&parsed).unwrap();
                    broadcast_message(&Message::Text(Into::into(json)), &connections_clone, addr)
                        .await;
                } else {
                    eprintln!("Invalid JSON format received: {}", text);
                }
            }
        }
    });

    tokio::select! {
        _ = send_task => {},
        _ = recv_task => {},
    }

    {
        let mut conn = connections.lock().await;
        conn.remove(&addr);
    }
}

async fn broadcast_message(message: &Message, connections: &Connections, sender_addr: SocketAddr) {
    let conns = connections.lock().await;
    for (addr, tx) in conns.iter() {
        if addr != &sender_addr {
            let _ = tx.send(message.clone());
        }
    }
}