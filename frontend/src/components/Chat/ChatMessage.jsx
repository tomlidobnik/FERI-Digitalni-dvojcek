import React from "react";

const ChatMessage = ({ msg, currentUsername }) => {
    const isoDate = msg.date
        ? msg.date.replace(" ", "T").replace(/$/, "Z")
        : "";

    const time = isoDate
        ? new Date(isoDate).toLocaleTimeString([], {
              hour: "2-digit",
              minute: "2-digit",
          })
        : "";

    const isCurrentUser = msg.username === currentUsername;

    return (
        <div
            className={`mb-3 flex ${
                isCurrentUser ? "justify-end" : "justify-start"
            }`}>
            <div
                className={`max-w-[70%] w-fit p-3 rounded-xl shadow ${
                    isCurrentUser
                        ? "bg-tertiary text-white rounded-br-none"
                        : "bg-secondary text-text-custom rounded-bl-none"
                }`}>
                {!isCurrentUser && (
                    <p className="text-xs text-quaternary font-semibold mb-1">
                        {msg.username}
                    </p>
                )}
                <p className="text-sm break-words">{msg.message}</p>
                {time && (
                    <p
                        className={`text-xs mt-1 ${
                            isCurrentUser
                                ? "text-primary/80"
                                : "text-quaternary/80"
                        } text-right`}>
                        {time}
                    </p>
                )}
            </div>
        </div>
    );
};

export default ChatMessage;
