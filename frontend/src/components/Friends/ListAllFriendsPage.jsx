import { useState, useEffect } from "react";
import ChatBox from "../Chat/FriendChat";
import { Link } from "react-router-dom";
import ListUsers from "../Chat/ListUsers";
import Cookies from "js-cookie";
import FriendChat from "../Chat/FriendChatFull";

const ListAllFriendsPage = () => {
    const API_URL = import.meta.env.VITE_API_URL;

    const [searchValue, setSearchValue] = useState("");
    const [selectedMode, setSelectedMode] = useState(true);
    const [response, setResponse] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [chattingWithFriendId, setChattingWithFriendId] = useState(null);
    const [chattingWithFriendName, setChattingWithFriendName] = useState("");
    const [searchQuery, setSearchQuery] = useState("");

    useEffect(() => {
        setIsLoading(true);
        const token = Cookies.get("token");
        fetch(`https://${API_URL}/api/user/friends/list_ids`, {
            headers: {
                Authorization: token ? `Bearer ${token}` : "",
            },
        })
            .then((res) => res.json())
            .then((data) => setResponse(data))
            .catch((err) => {
                console.error(err);
                setResponse([]);
            })
            .finally(() => setIsLoading(false));
        console.log(response);
    }, [API_URL]);

    function handleSearchChange(query) {
        setSearchQuery(query.target.value);
    }

    function handleModeClick(mode) {
        setSelectedMode(mode);
    }

    const openChat = (friendId, friendName) => {
        setChattingWithFriendId(friendId);
        setChattingWithFriendName(friendName);
        setShowChatBox(true);
    };

    const closeChat = () => {
        setShowChatBox(false);
        setChattingWithFriendId(null);
    };

    return (
        <div className="flex flex-col md:flex-row h-full w-full bg-linear-to-t to-primary from-quaternary/35 md:from-quaternary/50 lg:bg-none lg:bg-primary md:rounded-2xl md:shadow-2xl p-2 xl:p-4">
            {/* Left: Friends List */}
            <div className="w-full md:w-1/2 flex flex-col h-full md:pr-2">
                <h1 className="flex flex-col text-2xl xl:text-3xl font-bold text-text mb-4">
                    Prijatelji
                </h1>
                <div className="flex flex-col gap-2 mb-6">
                    <input
                        type="text"
                        placeholder="Išči po uporabniškem imenu..."
                        value={searchQuery}
                        onChange={handleSearchChange}
                        className="p-3 rounded-xl border-0 border-none shadow-lg text-2xl bg-white/40 text-text font-semibold w-full"
                    />
                    <div className="flex flex-wrap gap-2 mt-1">
                        <button
                            className={`px-3 py-1 rounded-lg ${
                                selectedMode ? "bg-black/40" : "bg-black/10"
                            } hover:bg-black/30 text-xs font-semibold `}
                            onClick={() => handleModeClick(true)}>
                            <div className="text-lg font-bold text-text">
                                Vsi
                            </div>
                        </button>
                        <button
                            className={`px-3 py-1 rounded-lg ${
                                !selectedMode ? "bg-black/40" : "bg-black/10"
                            } hover:bg-black/30 text-xs font-semibold `}
                            onClick={() => handleModeClick(false)}>
                            <div className="text-lg font-bold text-text">
                                Prijatelji
                            </div>
                        </button>
                    </div>
                </div>
                {isLoading ? (
                    <div className="flex-1 flex items-center justify-center h-full">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text-custom"></div>
                    </div>
                ) : (
                    <div className="flex-1 flex flex-col overflow-y-auto min-h-0">
                        <ListUsers selectedMode={selectedMode} onOpenChat={openChat} searchQuery={searchQuery}/>
                    </div>
                )}
            </div>
            {/* Right: Chat Box */}
            <div className="hidden md:flex w-1/2 flex-col h-full bg-white/40 rounded-2xl shadow-lg p-4 ml-0 md:ml-2">
                {/* You can render <ChatBox /> here based on selected friend */}
                <FriendChat
                    friendId={chattingWithFriendId}
                    friendName={chattingWithFriendName}
                    className="z-50"
                    onClose={closeChat}
                />
            </div>
        </div>
    );
};

export default ListAllFriendsPage;