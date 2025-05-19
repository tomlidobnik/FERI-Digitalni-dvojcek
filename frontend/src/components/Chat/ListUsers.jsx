import { useEffect, useState } from "react";
import Cookies from "js-cookie";
import { jwtDecode } from "jwt-decode";

const ListUsers = () => {
    const [users, setUsers] = useState([]);
    const [myUsername, setMyUsername] = useState("");
    const [requestStatus, setRequestStatus] = useState({});
    const [friendStatuses, setFriendStatuses] = useState({});

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        const token = Cookies.get("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setMyUsername(decoded.sub || decoded.username || "");
            } catch (err) {
                setMyUsername("");
            }
        }
    }, []);

    useEffect(() => {
        fetch(`https://${API_URL}/api/user/all`)
            .then((res) => res.json())
            .then((data) => setUsers(data))
            .catch((err) => {
                setUsers([]);
                console.error(err);
            });
    }, [API_URL]);

    useEffect(() => {
        const token = Cookies.get("token");
        users
            .filter((user) => user.username !== myUsername)
            .forEach((user) => {
                fetch(
                    `https://${API_URL}/api/user/friends/status/${user.username}`,
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
                            [user.username]: data.status,
                        }));
                    })
                    .catch(() => {
                        setFriendStatuses((prev) => ({
                            ...prev,
                            [user.username]: "Status: error",
                        }));
                    });
            });
    }, [users, myUsername, API_URL]);

    const sendFriendRequest = (username) => {
        const token = Cookies.get("token");
        fetch(`https://${API_URL}/api/user/friends/request`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: token ? `Bearer ${token}` : "",
            },
            body: JSON.stringify({ username }),
        })
            .then((res) => {
                if (res.ok) {
                    setRequestStatus((prev) => ({
                        ...prev,
                        [username]: "Request sent!",
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
                                [username]: data.status,
                            }));
                        });
                } else {
                    return res.json().then((data) => {
                        setRequestStatus((prev) => ({
                            ...prev,
                            [username]: data.error || "Request failed",
                        }));
                    });
                }
            })
            .catch(() => {
                setRequestStatus((prev) => ({
                    ...prev,
                    [username]: "Request failed",
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
                        [username]: "Removed",
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
                                [username]: data.status,
                            }));
                        });
                } else {
                    setRequestStatus((prev) => ({
                        ...prev,
                        [username]: "Remove failed",
                    }));
                }
            })
            .catch(() => {
                setRequestStatus((prev) => ({
                    ...prev,
                    [username]: "Remove failed",
                }));
            });
    };

    return (
        <div>
            <h3 className="text-lg font-bold mb-2">All Users</h3>
            <ul className="list-disc pl-5">
                {users
                    .filter((user) => user.username !== myUsername)
                    .map((user) => {
                        const status = friendStatuses[user.username];
                        const isPending =
                            status && status.toLowerCase().includes("pending");
                        const isFriend = status === "Friends";
                        const isNotFriend = status === "Not Friends";
                        let buttonText = "Add Friend";
                        let buttonAction = () =>
                            sendFriendRequest(user.username);
                        let buttonColor = "bg-blue-500";
                        let buttonDisabled = false;

                        if (isFriend) {
                            buttonText = "Remove Friend";
                            buttonAction = () =>
                                removeFriendOrRequest(user.username);
                            buttonColor = "bg-red-500";
                        } else if (isPending) {
                            buttonText = "Remove Request";
                            buttonAction = () =>
                                removeFriendOrRequest(user.username);
                            buttonColor = "bg-yellow-500";
                        } else if (!isNotFriend) {
                            buttonDisabled = true;
                        }

                        return (
                            <li
                                key={user.id ?? user.username}
                                className="mb-2 flex items-center gap-2">
                                {user.username}
                                <button
                                    className={`ml-2 px-2 py-1 text-white rounded disabled:opacity-50 ${buttonColor}`}
                                    onClick={buttonAction}
                                    disabled={buttonDisabled}>
                                    {buttonText}
                                </button>
                                <span className="ml-2 text-yellow-600">
                                    {status
                                        ? `Status: ${status}`
                                        : "Status: ..."}
                                </span>
                                {requestStatus[user.username] && (
                                    <span className="ml-2 text-green-600">
                                        {requestStatus[user.username]}
                                    </span>
                                )}
                            </li>
                        );
                    })}
            </ul>
        </div>
    );
};

export default ListUsers;
