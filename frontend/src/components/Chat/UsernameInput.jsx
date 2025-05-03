import React from "react";

const UsernameInput = ({ username, setUsername, isConnected }) => (
    <input
        type="text"
        placeholder="Enter username"
        className="flex-1 px-4 py-2 border border-gray-600 rounded disabled:opacity-50"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        disabled={isConnected}
    />
);

export default UsernameInput;
