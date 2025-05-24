import { useState, useEffect } from "react";
import EventForListDetail from "./EventForListDetail";
import { Link } from "react-router-dom";

const ListAllEventsDetail = ({ selectMode }) => {
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

    const now = new Date();
    const filteredEvents = response.filter(event => {
        const start = new Date(event.start_date);
        const end = new Date(event.end_date);

        if (selectMode === 0) return true;
        if (selectMode === 1) return start <= now && end >= now;
        if (selectMode === 2) return start > now;
        if (selectMode === 3) return end < now;
        if (selectMode === 4) return true; //TODO
        return true;
    });

    return (
        <div className="w-full h-full overflow-y-auto p-4 xl:p-6">
            <h1 className="text-2xl xl:text-5xl font-bold text-text mb-4 ">Dogodki</h1>
            <div className="flex flex-col ">
                {filteredEvents.length === 0 ? (
                    <>
                        <div className="text-center text-text/70 text-lg py-8">Ni dogodkov za prikaz.</div>
                    </>
                ) : (
                    filteredEvents.map((event) => (
                        <EventForListDetail
                            key={event.id}
                            event={event}
                        />
                    ))
                )}
            </div>
        </div>
    );
};

export default ListAllEventsDetail;