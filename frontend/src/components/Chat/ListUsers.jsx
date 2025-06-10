import { useEffect, useState, useMemo } from "react";
import Cookies from "js-cookie";
import { jwtDecode } from "jwt-decode";
import FriendChat from "./FriendChat";
import UserForList from "./UserForList";

const ListUsers = ({selectedMode, onOpenChat, searchQuery}) => {
    const [users, setUsers] = useState([]);
    const [myUsername, setMyUsername] = useState("");
    const [requestStatus, setRequestStatus] = useState({});
    const [friendStatuses, setFriendStatuses] = useState({});
    const [chattingWithFriendId, setChattingWithFriendId] = useState(null);
    const [chattingWithFriendName, setChattingWithFriendName] = useState("");
    const [showChatBox, setShowChatBox] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        const token = Cookies.get("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setMyUsername(decoded.sub || decoded.username || "");
            } catch (err) {
                setMyUsername("");
                console.error("Failed to decode token: ", err);
            }
        }
    }, []);

    useEffect(() => {
        setIsLoading(true);
        fetch(`https://${API_URL}/api/user/all`)
            .then((res) => res.json())
            .then((data) => {
                setUsers(data.filter((user) => user.username !== myUsername));
            })
            .catch((err) => {
                setUsers([]);
                console.error("Failed to fetch users: ", err);
            })
            .finally(() => setIsLoading(false));
    }, [API_URL, myUsername]);

    useEffect(() => {
        if (!myUsername || users.length === 0) {
            if (users.length > 0) setIsLoading(false);
            return;
        }

        const token = Cookies.get("token");
        const usersToFetchStatusFor = users.filter(
            (user) => user.username !== myUsername
        );

        if (usersToFetchStatusFor.length === 0) {
            setIsLoading(false);
            return;
        }

        Promise.all(
            usersToFetchStatusFor.map((user) =>
                fetch(
                    `https://${API_URL}/api/user/friends/status/${user.username}`,
                    {
                        headers: {
                            Authorization: token ? `Bearer ${token}` : "",
                        },
                    }
                )
                    .then((res) => res.json())
                    .then((data) => ({
                        username: user.username,
                        statusData: data,
                    }))
                    .catch(() => ({
                        username: user.username,
                        statusData: { status: "Status: error" },
                    }))
            )
        )
            .then((results) => {
                const newFriendStatuses = {};
                results.forEach((result) => {
                    newFriendStatuses[result.username] = result.statusData;
                });
                setFriendStatuses((prev) => ({
                    ...prev,
                    ...newFriendStatuses,
                }));
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [users, myUsername, API_URL]);

    const sendFriendRequest = (username) => {
        const token = Cookies.get("token");
        const previousFriendStatus = friendStatuses[username];

        setRequestStatus((prev) => {
            const newReqStatus = { ...prev };
            delete newReqStatus[username];
            return newReqStatus;
        });

        fetch(`https://${API_URL}/api/user/friends/request`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: token ? `Bearer ${token}` : "",
            },
            body: JSON.stringify({ username }),
        })
            .then(async (res) => {
                if (res.ok) {
                    try {
                        const statusRes = await fetch(
                            `https://${API_URL}/api/user/friends/status/${username}`,
                            {
                                headers: {
                                    Authorization: token
                                        ? `Bearer ${token}`
                                        : "",
                                },
                            }
                        );
                        if (statusRes.ok) {
                            const data = await statusRes.json();
                            setFriendStatuses((prev) => ({
                                ...prev,
                                [username]: data,
                            }));
                        } else {
                            console.error(
                                "Failed to re-fetch authoritative status for",
                                username,
                                "after successful request."
                            );
                        }
                    } catch (statusErr) {
                        console.error("Error re-fetching status:", statusErr);
                    }
                } else {
                    const errorData = await res.json().catch(() => ({
                        error: "Neznana napaka pri pošiljanju",
                    }));
                    setRequestStatus((prev) => ({
                        ...prev,
                        [username]:
                            errorData.error || "Pošiljanje zahteve ni uspelo",
                    }));
                    setFriendStatuses((prev) => ({
                        ...prev,
                        [username]: previousFriendStatus || {
                            status: "Not friends",
                            friendship_id: null,
                        },
                    }));
                }
            })
            .catch((err) => {
                console.error(
                    "Network or other error in sendFriendRequest:",
                    err
                );
                setRequestStatus((prev) => ({
                    ...prev,
                    [username]: "Napaka v omrežju ali druga težava",
                }));
                setFriendStatuses((prev) => ({
                    ...prev,
                    [username]: previousFriendStatus || {
                        status: "Not friends",
                        friendship_id: null,
                    },
                }));
            });
    };

    const removeFriendOrRequest = (username) => {
        const token = Cookies.get("token");
        fetch(`https://${API_URL}/api/user/friends/remove/${username}`, {
            method: "DELETE",
            headers: {
                Authorization: token ? `Bearer ${token}` : "",
            },
        })
            .then((res) => {
                if (res.ok) {
                    setRequestStatus((prev) => ({
                        ...prev,
                        [username]: "Odstranjeno",
                    }));
                    fetch(
                        `https://${API_URL}/api/user/friends/status/${username}`,
                        {
                            headers: {
                                Authorization: token ? `Bearer ${token}` : "",
                            },
                        }
                    )
                        .then((res) => res.json())
                        .then((data) => {
                            setFriendStatuses((prev) => ({
                                ...prev,
                                [username]: data,
                            }));
                        });
                } else {
                    setRequestStatus((prev) => ({
                        ...prev,
                        [username]: "Odstranjevanje ni uspelo",
                    }));
                }
            })
            .catch(() => {
                setRequestStatus((prev) => ({
                    ...prev,
                    [username]: "Odstranjevanje ni uspelo",
                }));
            });
    };

    const openChat = (friendId, friendName) => {
        onOpenChat(friendId, friendName)
    };

    const closeChat = () => {
        setShowChatBox(false);
        setChattingWithFriendId(null);
        setChattingWithFriendName("");
    };

    const displayedUsers = useMemo(() => {
        setIsLoading(true);
        return users
            .filter((user) => user.username !== myUsername)
            .filter((user) => {
                if (selectedMode === false) {
                    return friendStatuses[user.username]?.status === "Friends";
                }
                return true;
            })
            .filter((user) => {

                if (!searchQuery) return true;
                return user.username
                    .toLowerCase()
                    .includes(searchQuery.toLowerCase());
            })
            .sort((a, b) => {
                const aStatus = friendStatuses[a.username]?.status;
                const bStatus = friendStatuses[b.username]?.status;

                if (aStatus === "Friends" && bStatus !== "Friends") return -1;
                if (aStatus !== "Friends" && bStatus === "Friends") return 1;
                return 0;
            });
    }, [users, myUsername, selectedMode, friendStatuses, searchQuery]);

    useEffect(() => {
        const timeout = setTimeout(() => {
            setIsLoading(false);
        }, 0);

        return () => clearTimeout(timeout);
    }, [displayedUsers]);

    return (
        <div className="flex flex-col h-full overflow-y-auto">
            <div className="flex-1 flex flex-col min-h-0 overflow-y-auto">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-quaternary"></div>
                    </div>
                ) : (
                    <>
                        {displayedUsers.length === 0 ? (
                            <div className="text-center text-text-custom/70 text-lg py-8">
                                Ni uporabnikov za prikaz.
                            </div>
                        ) : (
                            displayedUsers.map((user) => (
                                <UserForList
                                    key={user.id ?? user.username}
                                    user={user}
                                    myUsername={myUsername}
                                    friendStatusObject={
                                        friendStatuses[user.username]
                                    }
                                    requestStatus={requestStatus}
                                    onSendFriendRequest={sendFriendRequest}
                                    onRemoveFriendOrRequest={
                                        removeFriendOrRequest
                                    }
                                    onOpenChat={openChat}
                                />
                            ))
                        )}
                    </>
                )}
            </div>
            {showChatBox && chattingWithFriendId && (
                <FriendChat
                    friendId={chattingWithFriendId}
                    friendName={chattingWithFriendName}
                    className="z-50"
                    onClose={closeChat}
                />
            )}
        </div>
    );
};

export default ListUsers;
