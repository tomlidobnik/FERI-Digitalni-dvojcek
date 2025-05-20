import { useState, useEffect } from "react";
import ChatBox from "../Chat/FriendChat";
import { Link } from "react-router-dom";

const ListAllFriends = () => {
    const API_URL = import.meta.env.VITE_API_URL;

    const [response, setResponse] = useState([]);

    useEffect(() => {
        fetch(`https://${API_URL}/api/user/friends/list_ids`)
            .then((res) => res.json())
            .then((data) => setResponse(data))
            .catch((err) => {
                console.error(err);
            });
    }, [API_URL]);

    return (
        <div className="bg-primary w-full md:rounded-2xl shadow-2xl p-4 xl:p-6 pt-16 md:pt-4 h-full overflow-y-auto pb-8 md:pb-0">
            <h1 className="text-3xl lg:text-4xl font-bold text-text mb-4 ">Chat</h1>
            <div className="flex flex-col items-center justify-center">
                {response.length === 0 ? (
                    <div className="flex flex-col items-center justify-center w-64">
                        <div className="text-center text-text/70 text-lg py-8">Ni prijateljev</div>
                        <Link to="/add_friends" className="btn-nav">Dodaj prijatelje</Link>
                    </div>
                ) : (
                    <ChatBox/>
                )}
            </div>
        </div>
    );
};

export default ListAllFriends;