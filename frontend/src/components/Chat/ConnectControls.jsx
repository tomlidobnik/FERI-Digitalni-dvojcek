import React from "react";

const ConnectControls = ({
    isConnected,
    username,
    handleConnect,
    handleDisconnect,
}) => (
    <>
        <button
            onClick={handleConnect}
            disabled={isConnected || !username.trim()}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded disabled:opacity-50">
            Connect
        </button>
        <button
            onClick={handleDisconnect}
            disabled={!isConnected}
            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded disabled:opacity-50">
            Disconnect
        </button>
    </>
);

export default ConnectControls;
