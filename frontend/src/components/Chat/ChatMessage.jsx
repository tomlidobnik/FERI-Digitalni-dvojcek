import React from "react";

const ChatMessage = ({ msg }) => {
    const isoDate = msg.date
        ? msg.date.replace(" ", "T").replace(/$/, "Z")
        : "";

    const time = isoDate
        ? new Date(isoDate).toLocaleTimeString([], {
              hour: "2-digit",
              minute: "2-digit",
          })
        : "";

    return (
        <div className="mb-4 text-left">
            <div className="flex items-center gap-2">
                <p className="text-sm text-orange-400 font-semibold">
                    {msg.username}
                </p>
                {time && <span className="text-xs text-gray-400">{time}</span>}
            </div>
            <div className="bg-blue-600 text-white px-4 py-2 rounded-lg max-w-xs break-words w-fit">
                {msg.message}
            </div>
        </div>
    );
};

export default ChatMessage;
