import React from "react";
import { Link } from "react-router-dom";

const FriendsForList = ({
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
        <div className="bg-white/30 p-2 rounded-2xl shadow-md mb-4 text-text-custom">
            <div className="flex flex-col sm:flex-row items-center">
                <Link to={"/profile/"+user.id} className="flex items-center ml-2">
                    <div className="w-8 h-8 rounded-full bg-quaternary flex items-center justify-center text-white text-lg font-bold shadow mr-2">
                        {user ? <>{user.username ? user.username.charAt(0).toUpperCase() : "U"}</>: "U"}
                    </div>
                    <span className="font-semibold text-lg text-black truncate">
                        {user.username}
                    </span>
                </Link>
                {isFriend && friendshipId && (
                    <button
                        className="md:ml-auto flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 md:p-3 md:px-6 xl:px-8 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
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

export default FriendsForList;
