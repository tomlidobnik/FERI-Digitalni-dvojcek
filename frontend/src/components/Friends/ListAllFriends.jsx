import { useState, useEffect } from "react";
import ChatBox from "../Chat/FriendChat";
import { Link } from "react-router-dom";
import ListUsers from "../Chat/ListUsers";
import Cookies from "js-cookie";

const ListAllFriends = () => {
    const API_URL = import.meta.env.VITE_API_URL;

    const [response, setResponse] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

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

    return (
        <div className="flex flex-col h-full bg-linear-to-t to-primary from-quaternary/35 md:from-quaternary/50 lg:bg-none lg:bg-primary w-full md:rounded-2xl md:shadow-2xl p-2 xl:p-4">
            <h1 className="flex flex-col text-2xl xl:text-3xl font-bold text-text mb-4">
                Prijatelji
            </h1>
            {isLoading ? (
                <div className="flex-1 flex items-center justify-center h-full">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text-custom"></div>
                </div>
            ):(
                <div className="flex-1 flex flex-col overflow-y-auto min-h-0">
                    <ListUsers />
                </div>
            )}
        </div>
    );
};

export default ListAllFriends;
