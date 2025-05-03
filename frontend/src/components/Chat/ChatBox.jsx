import React, { useState, useRef } from "react";
import UsernameInput from "./UsernameInput";
import ConnectControls from "./ConnectControls";
import ChatMessages from "./ChatMessages";
import ChatInput from "./ChatInput";

const ChatBox = () => {
    const [username, setUsername] = useState("");
    const [message, setMessage] = useState("");
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState([]);
    const socketRef = useRef(null);

    const handleConnect = () => {
        if (!isConnected && username.trim()) {
            const socket = new WebSocket("ws://localhost:8000/ws");
            socketRef.current = socket;

            socket.onopen = () => setIsConnected(true);
            socket.onmessage = (event) => {
                const received = JSON.parse(event.data);
                setMessages((prev) => [...prev, received]);
            };
            socket.onerror = (err) => console.error("WebSocket error:", err);
            socket.onclose = () => setIsConnected(false);
        }
    };

    const handleDisconnect = () => {
        if (socketRef.current) {
            socketRef.current.close();
            setIsConnected(false);
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            const msg = { user: username, message };
            socketRef.current.send(JSON.stringify(msg));
            setMessages((prev) => [...prev, msg]);
            setMessage("");
        }
    };

    return (
        <div className="min-h-screen text-white p-6">
            <h2 className="text-2xl font-bold mb-4">WebSocket Chat</h2>
            <div className="flex items-center gap-3 mb-4">
                <UsernameInput
                    username={username}
                    setUsername={setUsername}
                    isConnected={isConnected}
                />
                <ConnectControls
                    isConnected={isConnected}
                    username={username}
                    handleConnect={handleConnect}
                    handleDisconnect={handleDisconnect}
                />
            </div>
            <ChatMessages messages={messages} />
            <ChatInput
                message={message}
                setMessage={setMessage}
                handleSubmit={handleSubmit}
                isConnected={isConnected}
            />
            {!isConnected && (
                <p className="mt-4 text-sm text-gray-400">
                    Not connected. Enter a username and click Connect.
                </p>
            )}
        </div>
    );
};

export default ChatBox;
