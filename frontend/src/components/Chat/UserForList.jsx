import React from "react";
import { Link } from "react-router-dom";

const UserForList = ({
    user,
    myUsername,
    friendStatusObject,
    requestStatus,
    onSendFriendRequest,
    onRemoveFriendOrRequest,
    onOpenChat,
}) => {
    const status = friendStatusObject?.status;
    const friendshipId = friendStatusObject?.friendship_id;

    const statusTranslations = {
        Friends: "Prijatelja",
        "Not Friends": "Nista prijatelja",
        "Accept Friend Request": "Sprejmi prošnjo za prijateljstvo",
    };

    let displayStatus = status;
    if (status && status.toLowerCase().includes("sent")) {
        displayStatus =
            statusTranslations["Pending outgoing"] || "Prošnja v teku";
    } else if (status && statusTranslations[status]) {
        displayStatus = statusTranslations[status];
    }

    const isPending = status && status.toLowerCase().includes("pending");
    const isFriend = status === "Friends";
    const isNotFriend = status === "Not Friends";
    const isAccept = status === "Accept Friend Request";

    let button1Text = "";
    let button1Action = () => {};
    let button1Styles = "bg-gray-400 text-white";
    let button2Text = "";
    let button2Action = () => {};
    let button2Styles = "bg-gray-400 text-white";
    let buttonDisabled = true;

    if (isFriend) {
        button1Text = "Odstrani prijatelja";
        button1Action = () => onRemoveFriendOrRequest(user.username);
        button1Styles =
            "bg-error text-white hover:bg-error/90 focus:ring-error/50";
        buttonDisabled = false;
    } else if (isPending) {
        button1Text = "Prekliči zahtevo";
        button1Action = () => onRemoveFriendOrRequest(user.username);
        button1Styles =
            "bg-warning text-text-custom hover:bg-warning/90 focus:ring-warning/50";
        buttonDisabled = false;
    } else if (isAccept) {
        button1Text = "Sprejmi prošnjo";
        button1Action = () => onSendFriendRequest(user.username);
        button1Styles =
            "bg-tertiary text-white hover:bg-tertiary/90 focus:ring-tertiary/50";
        button2Text = "Zavrni prošnjo";
        button2Action = () => onRemoveFriendOrRequest(user.username);
        button2Styles =
            "bg-error text-white hover:bg-error/90 focus:ring-error/50";
        buttonDisabled = false;
    } else if (isNotFriend) {
        button1Text = "Dodaj prijatelja";
        button1Action = () => onSendFriendRequest(user.username);
        button1Styles =
            "bg-quaternary text-white hover:bg-quaternary/90 focus:ring-quaternary/50";
        buttonDisabled = false;
    } else {
        button2Text = "Zavrni prošnjo";
        button2Action = () => onRemoveFriendOrRequest(user.username);
        button2Styles =
            "bg-error text-white hover:bg-error/90 focus:ring-error/50";
        buttonDisabled = false;
    }

    return (
        <div className="bg-white/30 p-4 rounded-2xl shadow-md mb-4 text-text-custom">
            <div className="flex flex-col sm:flex-row sm:items-center gap-3">
                <Link to={"/profile/"+user.id} className="flex items-center">
                    <div className="w-10 h-10 rounded-full bg-quaternary flex items-center justify-center text-white text-lg font-bold shadow mr-2">
                        {user ? <>{user.username ? user.username.charAt(0).toUpperCase() : "U"}</>: "U"}
                    </div>
                    <span className="font-semibold text-lg flex items-center text-black truncate">
                        {user.username}
                    </span>
                </Link>
                <span
                    className={`text-xs sm:text-sm font-medium px-2 py-1 mr-auto rounded-full sm:mt-0
                    ${
                        isFriend
                            ? "bg-tertiary/20 text-quaternary"
                            : isPending
                            ? "bg-warning/20 text-warning"
                            : isAccept
                            ? "bg-secondary/20 text-secondary"
                            : isNotFriend
                            ? "bg-quaternary/20 text-quaternary"
                            : "bg-gray-500/20 text-gray-500"
                    }`}>
                    {displayStatus
                        ? displayStatus
                        : friendStatusObject === undefined
                        ? "Nalaganje statusa..."
                        : "Status ni znan"}
                </span>
                {button1Text && (
                    <button
                        className={`px-3 py-1.5 text-sm font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 ${button1Styles}`}
                        onClick={button1Action}
                        disabled={
                            buttonDisabled ||
                            status === null ||
                            (typeof status === "undefined" &&
                                button1Text === "Osveži status")
                        }>
                        {button1Text}
                    </button>
                )}
                {button2Text && (
                    <button
                        className={`px-3 py-1.5 h-8 text-sm font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 ${button2Styles}`}
                        onClick={button2Action}
                        disabled={
                            buttonDisabled ||
                            status === null ||
                            typeof status === "undefined"
                        }>
                        {button2Text}
                    </button>
                )}
                {isFriend && friendshipId && (
                    <button
                        className=" flex-shrink-0 text-right bg-black/10 rounded-2xl p-3 px-4 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                        onClick={() => onOpenChat(friendshipId, user.username)}
                        disabled={
                            !friendshipId ||
                            status === null ||
                            typeof status === "undefined"
                        }>
                        <img
                            src="icons/chat.svg"
                            className="w-4 h-4 md:w-8 md:h-8 transition-transform duration-300 group-hover:scale-125"
                            alt="details"
                        />
                    </button>
                )}
            </div>
        </div>
    );
};

export default UserForList;
