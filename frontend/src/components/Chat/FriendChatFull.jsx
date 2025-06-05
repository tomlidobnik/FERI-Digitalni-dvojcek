import { useState, useRef, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import ChatMessages from "./ChatMessages";
import ChatInput from "./ChatInput";
import Cookies from "js-cookie";
import { Link } from "react-router-dom";

const ChatBox = ({ friendId, friendName, onClose }) => {
    const [currentUsername, setCurrentUsername] = useState("");
    const [message, setMessage] = useState("");
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState([]);
    const [showContent, setShowContent] = useState(false);
    const socketRef = useRef(null);
    const messagesContainerRef = useRef(null);

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        const timer = setTimeout(() => setShowContent(true), 50);
        return () => clearTimeout(timer);
    }, []);

    useEffect(() => {
        const token = Cookies.get("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setCurrentUsername(decoded.sub || decoded.username || "");
            } catch (err) {
                setCurrentUsername("");
            }
        }
    }, []);

    useEffect(() => {
        if (messagesContainerRef.current) {
            messagesContainerRef.current.scrollTop =
                messagesContainerRef.current.scrollHeight;
        }
    }, [messages]);

    useEffect(() => {
        if (!friendId) {
            if (socketRef.current) {
                socketRef.current.close();
                socketRef.current = null;
                setIsConnected(false);
            }
            return;
        }

        const token = Cookies.get("token");
        let usernameForSocketUrl = "";
        if (token) {
            try {
                const decoded = jwtDecode(token);
                usernameForSocketUrl = decoded.sub || decoded.username || "";
            } catch (err) {}
        }

        const newSocket = new WebSocket(
            `wss://${API_URL}/ws/friend/${friendId}?token=${token}&username=${encodeURIComponent(
                usernameForSocketUrl
            )}`
        );
        socketRef.current = newSocket;

        newSocket.onopen = () => {
            setIsConnected(true);
            fetch(`https://${API_URL}/api/chat/friend_history/${friendId}`, {
                headers: {
                    Authorization: token ? `Bearer ${token}` : "",
                },
            })
                .then((res) => res.json())
                .then((data) => {
                    setMessages(data);
                })
                .catch((err) => {});
        };

        newSocket.onmessage = (event) => {
            try {
                const received = JSON.parse(event.data);
                setMessages((prev) => [...prev, received]);
            } catch (e) {}
        };

        newSocket.onerror = (err) => {};

        newSocket.onclose = (event) => {
            if (socketRef.current === newSocket) {
                setIsConnected(false);
                socketRef.current = null;
            }
        };

        return () => {
            if (
                newSocket.readyState === WebSocket.OPEN ||
                newSocket.readyState === WebSocket.CONNECTING
            ) {
                newSocket.close();
            }
            if (socketRef.current === newSocket) {
                socketRef.current = null;
            }
        };
    }, [friendId, API_URL]);

    const handleActualClose = () => {
        setShowContent(false);
        setTimeout(() => {
            onClose();
        }, 500);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            const token = Cookies.get("token");
            let usernameFromToken = "";
            if (token) {
                try {
                    const decoded = jwtDecode(token);
                    usernameFromToken = decoded.sub || decoded.username || "";
                } catch (err) {}
            }
            const now = new Date();
            const dateString = now.toISOString().replace("T", " ").slice(0, 19);
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
        <div
            className={`w-full h-full rounded-lg p-4 flex flex-col text-text-custom `}>
            <Link
                to={friendId !== null ? `/profile/${friendId}` : "#"}
                className="flex justify-between items-center mb-2 h-14 w-fit"
            >
                <h3 className="flex items-center text-lg font-semibold text-quaternary">
                    {friendName ? (
                        <div className="w-10 h-10 rounded-full bg-quaternary flex items-center justify-center text-white text-lg font-bold shadow mr-2">
                            {friendName.charAt(0).toUpperCase()}
                        </div>
                    ) : ""}
                    {friendName || `Prosim izberite prijatelja za klepet.`}
                </h3>
            </Link>
            <ChatMessages
                css={""}
                messages={messages}
                containerRef={messagesContainerRef}
                currentUsername={currentUsername}
            />
            <ChatInput
                css={"bg-transparent"}
                message={message}
                setMessage={setMessage}
                handleSubmit={handleSubmit}
                isConnected={isConnected}
            />
        </div>
    );
};

export default ChatBox;
