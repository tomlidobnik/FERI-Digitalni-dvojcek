import { useState, useRef, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import ChatMessages from "./ChatMessages";
import ChatInput from "./ChatInput";
import Cookies from "js-cookie";

const ChatBox = () => {
    const [username, setUsername] = useState("");
    const [message, setMessage] = useState("");
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState([]);
    const [friends, setFriends] = useState([]);
    const [selectedFriendId, setSelectedFriendId] = useState("");
    const socketRef = useRef(null);
    const messagesContainerRef = useRef(null);

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        if (messagesContainerRef.current) {
            messagesContainerRef.current.scrollTop =
                messagesContainerRef.current.scrollHeight;
        }
    }, [messages]);

    useEffect(() => {
        const token = Cookies.get("token");
        fetch(`https://${API_URL}/api/user/friends/list_ids`, {
            headers: {
                Authorization: token ? `Bearer ${token}` : "",
            },
        })
            .then((res) => {
                if (!res.ok) throw new Error("Unauthorized");
                return res.json();
            })
            .then((data) => {
                setFriends(data);
                if (data.length > 0) setSelectedFriendId(data[0]);
            })
            .catch((err) => {
                setFriends([]);
                setSelectedFriendId("");
                console.error(err);
            });
    }, [API_URL]);

    useEffect(() => {
        if (!selectedFriendId) return;

        if (socketRef.current) {
            socketRef.current.close();
            setIsConnected(false);
        }

        const socket = new WebSocket(
            `wss://${API_URL}/ws/friend/${selectedFriendId}`
        );
        socketRef.current = socket;

        socket.onopen = () => {
            setIsConnected(true);
            const token = Cookies.get("token");
            fetch(
                `https://${API_URL}/api/chat/friend_history/${selectedFriendId}`,
                {
                    headers: {
                        Authorization: token ? `Bearer ${token}` : "",
                    },
                }
            )
                .then((res) => res.json())
                .then((data) => {
                    setMessages(data);
                });
        };
        socket.onmessage = (event) => {
            const received = JSON.parse(event.data);
            setMessages((prev) => [...prev, received]);
        };
        socket.onerror = (err) => console.error("WebSocket error:", err);
        socket.onclose = () => setIsConnected(false);

        return () => {
            if (socketRef.current) {
                socketRef.current.close();
                setIsConnected(false);
            }
        };
    }, [selectedFriendId, API_URL]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            const token = Cookies.get("token");
            let usernameFromToken = "";
            if (token) {
                try {
                    const decoded = jwtDecode(token);
                    usernameFromToken = decoded.sub || decoded.username || "";
                } catch (err) {
                    console.error("Failed to decode token", err);
                }
            }
            const now = new Date();
            const dateString = now.toISOString().replace("T", " ").slice(0, 19); // "YYYY-MM-DD HH:MM:SS"
            const msg = {
                username: usernameFromToken,
                message,
                date: dateString,
            };
            socketRef.current.send(
                JSON.stringify({ username: usernameFromToken, message })
            );
            setMessages((prev) => [...prev, msg]);
            setMessage("");
        }
    };

    return (
        <div className="min-h-screen p-6 text-text">
            <h2 className="text-2xl font-bold mb-4">WebSocket Friend Chat</h2>
            <div className="flex items-center gap-3 mb-4">
                <select
                    className="px-2 py-1 border rounded"
                    value={selectedFriendId}
                    onChange={(e) => setSelectedFriendId(e.target.value)}
                    disabled={false}>
                    {friends.map((friendId) => (
                        <option key={friendId} value={friendId}>
                            Friend ID: {friendId}
                        </option>
                    ))}
                </select>
            </div>
            <ChatMessages
                messages={messages}
                containerRef={messagesContainerRef}
            />
            <ChatInput
                message={message}
                setMessage={setMessage}
                handleSubmit={handleSubmit}
                isConnected={isConnected}
            />
        </div>
    );
};

export default ChatBox;
