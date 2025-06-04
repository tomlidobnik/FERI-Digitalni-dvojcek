import React from "react";
import ChatMessage from "./ChatMessage";

const ChatMessages = ({ messages, containerRef, currentUsername }) => (
    <div
        ref={containerRef}
        className="h-full overflow-y-auto border border-quaternary/50 rounded-xl p-4 mb-4 bg-primary">
        {messages.map((msg, idx) => (
            <ChatMessage
                key={idx}
                msg={msg}
                currentUsername={currentUsername}
            />
        ))}
    </div>
);

export default ChatMessages;
