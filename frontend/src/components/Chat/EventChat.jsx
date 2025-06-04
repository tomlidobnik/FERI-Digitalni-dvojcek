import { useState, useEffect, useRef } from "react";
import { jwtDecode } from "jwt-decode";
import Cookies from "js-cookie";
import ChatMessages from "./ChatMessages";
import ChatInput from "./ChatInput";
import apiService from "../../utils/apiService";

const EventChat = ({eventId}) => {
    const [events, setEvents] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState("");
    const [messages, setMessages] = useState([]);
    const [message, setMessage] = useState("");
    const [isConnected, setIsConnected] = useState(false);
    const [currentUsername, setCurrentUsername] = useState("");
    const socketRef = useRef(null);
    const messagesContainerRef = useRef(null);

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        const token = Cookies.get("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setCurrentUsername(decoded.sub || decoded.username || "");
            } catch (err) {
                console.error("Failed to decode token", err);
                setCurrentUsername("");
            }
        }
        setSelectedEventId(eventId);
    }, []);

    useEffect(() => {
        apiService
            .get("/event/all")
            .then((response) => {
                const eventList = response?.data || response;
                setEvents(eventList || []);
            })
            .catch((error) => {
                console.error("Error fetching events:", error);
                setEvents([]);
            });
    }, []);

    useEffect(() => {
        if (messagesContainerRef.current) {
            messagesContainerRef.current.scrollTop =
                messagesContainerRef.current.scrollHeight;
        }
    }, [messages]);

    useEffect(() => {
        if (!selectedEventId) return;

        if (socketRef.current) {
            socketRef.current.close();
            setIsConnected(false);
        }

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

        const socket = new WebSocket(
            `wss://${API_URL}/ws/event/${selectedEventId}?token=${token}&username=${encodeURIComponent(
                usernameFromToken
            )}`
        );
        socketRef.current = socket;

        socket.onopen = () => {
            setIsConnected(true);
            fetch(
                `https://${API_URL}/api/chat/history/${selectedEventId}`,
                {
                    headers: {
                        Authorization: token ? `Bearer ${token}` : "",
                    },
                }
            )
                .then(async (res) => {
                    const text = await res.text();
                    if (!text) return [];
                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        console.error(
                            "Failed to parse chat history JSON:",
                            e,
                            text
                        );
                        return [];
                    }
                })
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
        if (
            socketRef.current?.readyState === WebSocket.OPEN &&
            currentUsername
        ) {
            const now = new Date();
            const dateString = now.toISOString().replace("T", " ").slice(0, 19);
            const msg = {
                username: currentUsername,
                message,
                date: dateString,
            };
            socketRef.current.send(
                JSON.stringify({ username: currentUsername, message })
            );
            setMessages((prev) => [...prev, msg]);
            setMessage("");
        }
    };

    return (
        <div className=" rounded-lg text-text-custom h-full flex flex-col">
            {selectedEventId ? (
                <>
                    <ChatMessages
                        messages={messages}
                        containerRef={messagesContainerRef}
                        currentUsername={currentUsername}
                    />
                    <ChatInput
                        message={message}
                        setMessage={setMessage}
                        handleSubmit={handleSubmit}
                        isConnected={isConnected}
                    />
                </>
            ) : (
                <div className="flex-grow flex items-center justify-center">
                    <p className="text-text-secondary">
                        Izberite dogodek za ogled klepeta.
                    </p>
                </div>
            )}
        </div>
    );
};

export default EventChat;