import React from "react";

const ChatInput = ({ message, setMessage, handleSubmit, isConnected }) => (
    <form onSubmit={handleSubmit} className="flex gap-3">
        <input
            type="text"
            placeholder="Type a message..."
            className="flex-1 px-4 py-2 border border-gray-600 rounded"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            disabled={!isConnected}
        />
        <button
            type="submit"
            disabled={!isConnected || !message.trim()}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded disabled:opacity-50">
            Send
        </button>
    </form>
);

export default ChatInput;
