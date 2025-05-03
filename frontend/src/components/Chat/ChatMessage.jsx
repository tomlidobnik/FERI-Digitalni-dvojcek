import React from "react";

const ChatMessage = ({ msg }) => (
    <div className="mb-4 text-left">
        <p className="text-sm text-orange-400 font-semibold">{msg.username}</p>
        <div className="bg-blue-600 text-white px-4 py-2 rounded-lg max-w-xs break-words w-fit">
            {msg.message}
        </div>
    </div>
);

export default ChatMessage;
