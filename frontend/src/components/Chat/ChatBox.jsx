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
    const [events, setEvents] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState("");
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
        fetch(`https://${API_URL}/api/event/available`)
            .then((res) => res.json())
            .then((data) => {
                setEvents(data);
                if (data.length > 0) setSelectedEventId(data[0].id);
            });
    }, [API_URL]);

    useEffect(() => {
        if (!selectedEventId) return;

        if (socketRef.current) {
            socketRef.current.close();
            setIsConnected(false);
        }

        const socket = new WebSocket(`wss://${API_URL}/ws/${selectedEventId}`);
        socketRef.current = socket;

        socket.onopen = () => {
            setIsConnected(true);
            fetch(`https://${API_URL}/api/chat/history/${selectedEventId}`)
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
    }, [selectedEventId, API_URL]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            const msg = { username: username, message };
            socketRef.current.send(JSON.stringify(msg));
            setMessages((prev) => [...prev, msg]);
            setMessage("");
        }
    };

    useEffect(() => {
        const token = Cookies.get("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setUsername(decoded.sub || decoded.username || "");
            } catch (e) {
                setUsername("");
            }
        }
    }, []);

    return (
        <div className="min-h-screen p-6 text-text">
            <h2 className="text-2xl font-bold mb-4">WebSocket Chat</h2>
            <div className="flex items-center gap-3 mb-4">
                <select
                    className="px-2 py-1 border rounded"
                    value={selectedEventId}
                    onChange={(e) => setSelectedEventId(e.target.value)}
                    disabled={false}>
                    {events.map((event) => (
                        <option key={event.id} value={event.id}>
                            {event.title} (ID: {event.id})
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
