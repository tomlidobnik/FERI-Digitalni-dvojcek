import React from "react";

const ChatInput = ({ message, setMessage, handleSubmit, isConnected }) => (
    <form onSubmit={handleSubmit} className="flex gap-3 mt-auto pt-2">
        <input
            type="text"
            placeholder="Napišite sporočilo..."
            className="flex-1 px-4 py-2 border border-quaternary/50 rounded-xl bg-primary text-text-custom focus:outline-none focus:ring-2 focus:ring-tertiary focus:border-transparent transition-colors duration-150"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            disabled={!isConnected}
        />
        <button
            type="submit"
            disabled={!isConnected || !message.trim()}
            className="bg-tertiary hover:brightness-90 text-white px-6 py-2 rounded-lg disabled:opacity-60 transition-all duration-150 shadow-md focus:outline-none focus:ring-2 focus:ring-tertiary/80 focus:ring-offset-2 focus:ring-offset-primary">
            Pošlji
        </button>
    </form>
);

export default ChatInput;
