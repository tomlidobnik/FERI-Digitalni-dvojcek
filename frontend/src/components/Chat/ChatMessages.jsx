import React from "react";
import ChatMessage from "./ChatMessage";

const ChatMessages = ({ css, messages, containerRef, currentUsername }) => (
    <div
        ref={containerRef}
        className={"overflow-y-auto border border-quaternary/50 rounded-xl p-4 mb-4  h-full w-full " + css}
    >
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