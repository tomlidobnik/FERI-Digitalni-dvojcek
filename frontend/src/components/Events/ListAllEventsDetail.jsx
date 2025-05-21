import { useState, useEffect } from "react";
import EventForListDetail from "./EventForListDetail";
import { Link } from "react-router-dom";

const ListAllEventsDetail = () => {
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
        <div className="w-full h-full overflow-y-auto p-4 xl:p-6">
            <h1 className="text-2xl xl:text-5xl font-bold text-text mb-4 ">Dogodki</h1>
            <div className="flex flex-col ">
                {response.length === 0 ? (
                    <>
                        <div className="text-center text-text/70 text-lg py-8">Ni dogodkov za prikaz.</div>
                    </>
                ) : (
                    response.map((event) => (
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