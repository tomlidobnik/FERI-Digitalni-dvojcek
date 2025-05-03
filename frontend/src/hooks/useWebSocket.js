import { useRef, useState, useCallback, useEffect } from "react";

export default function useWebSocket(url) {
    const socketRef = useRef(null);
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState([]);

    const connect = useCallback(() => {
        if (socketRef.current) return;
        const socket = new WebSocket(url);
        socketRef.current = socket;

        socket.onopen = () => setIsConnected(true);
        socket.onclose = () => {
            setIsConnected(false);
            socketRef.current = null;
        };
        socket.onmessage = (event) => {
            try {
                setMessages((prev) => [...prev, JSON.parse(event.data)]);
            } catch {
                // ignore invalid JSON
            }
        };
        socket.onerror = () => setIsConnected(false);
    }, [url]);

    const disconnect = useCallback(() => {
        socketRef.current?.close();
        socketRef.current = null;
        setIsConnected(false);
    }, []);

    const sendMessage = useCallback((msg) => {
        if (
            socketRef.current &&
            socketRef.current.readyState === WebSocket.OPEN
        ) {
            socketRef.current.send(JSON.stringify(msg));
        }
    }, []);

    return { isConnected, messages, connect, disconnect, sendMessage };
}
