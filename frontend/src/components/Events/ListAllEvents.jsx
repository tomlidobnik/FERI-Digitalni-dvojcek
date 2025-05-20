import { useState, useEffect } from "react";
import EventForList from "./EventForList";

const ListAllEvents = () => {
    const API_URL = import.meta.env.VITE_API_URL;

    const [response, setResponse] = useState([]);

    useEffect(() => {
        fetch(`https://${API_URL}/api/event/available`)
            .then((res) => res.json())
            .then((data) => setResponse(data))
            .catch((err) => {
                console.error(err);
            });
    }, [API_URL]);

    return (
        <div className="bg-primary w-full md:rounded-2xl shadow-2xl p-4 xl:p-6 h-full overflow-y-auto">
            <h1 className="text-3xl lg:text-4xl font-bold text-text mb-4 ">Seznam dogodkov</h1>
            <div className="flex flex-col ">
                {response.map((event) => (
                    <EventForList
                        event={event}
                    />
                ))}
            </div>
        </div>
    );
};

export default ListAllEvents;