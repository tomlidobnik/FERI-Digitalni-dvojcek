import React from "react";
import ChatMessage from "./ChatMessage";

const ChatMessages = ({ messages, containerRef }) => (
    <div
        ref={containerRef}
        className="h-96 overflow-y-auto border border-gray-700 rounded p-4 mb-4">
        {messages.map((msg, idx) => (
            <ChatMessage key={idx} msg={msg} />
        ))}
    </div>
);

export default ChatMessages;
