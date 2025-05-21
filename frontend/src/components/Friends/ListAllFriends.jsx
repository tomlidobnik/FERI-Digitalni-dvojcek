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
                {response.length === 0 ? (
                    <>
                        <h1 className="flex flex-col text-2xl xl:text-3xl font-bold text-text mb-4 h-1/12">Chat</h1>
                        <div className="flex flex-col items-center justify-center h-9/12">
                            <div className="flex flex-col items-center justify-center w-64 h-full">
                                <div className="text-center text-text/70 text-lg py-8">Ni prijateljev</div>
                                <Link to="/add_friends" className="btn-nav">Dodaj prijatelje</Link>
                            </div>
                        </div>
                    </>
                ) : (
                    <>
                        <h1 className="flex flex-col text-2xl xl:text-3xl font-bold text-text mb-4">Chat</h1>
                        <ChatBox/>
                    </>
                )}
        </div>
    );
};

export default ListAllFriends;