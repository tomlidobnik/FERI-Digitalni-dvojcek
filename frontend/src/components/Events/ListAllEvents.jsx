import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";

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
        <div className="bg-linear-to-t to-primary from-quaternary/35 md:from-quaternary/50 lg:bg-none lg:bg-primary w-full md:rounded-2xl md:shadow-2xl p-4 xl:p-6 h-full overflow-y-auto">
            <h1 className="text-2xl xl:text-3xl font-bold text-text mb-4 ">Dogodki</h1>
            <div className="flex flex-col ">
                {response.length === 0 ? (
                    <>
                        <div className="text-center text-text/70 text-lg py-8">Ni dogodkov za prikaz.</div>
                    </>
                ) : (
                    response.map((event) => (
                        <EventForList
                            key={event.id}
                            event={event}
                        />
                    ))
                )}
            </div>
        </div>
    );
};

export default ListAllEvents;