import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";

const ListAllEvents = () => {
    const API_URL = import.meta.env.VITE_API_URL;

    const [response, setResponse] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [allLocations, setAllLocations] = useState([]);

    useEffect(() => {
        setIsLoading(true);

        fetch(`https://${API_URL}/api/location/all`)
            .then(res => res.json())
            .then(data => {setAllLocations(data);})
            .catch(err => {
                console.error("Error fetching locations:", err);
                setAllLocations([]);
        });

        fetch(`https://${API_URL}/api/event/available`)
            .then((res) => res.json())
            .then((data) => {
                const sorted = data.sort((a, b) => new Date(a.start_date) - new Date(b.start_date));
                setResponse(sorted);
            })
            .catch((err) => {
                console.error(err);
            });
        setTimeout(() => setIsLoading(false), 300);
    }, [API_URL]);

    return (
        <div className="flex flex-col h-full bg-linear-to-t to-primary from-quaternary/35 md:from-quaternary/50 lg:bg-none lg:bg-primary w-full md:rounded-2xl md:shadow-2xl p-2 xl:p-4">
            <h1 className="text-2xl xl:text-3xl font-bold text-text mb-4">Aktualni dogodki</h1>
            <div className="flex-1 flex flex-col overflow-y-auto min-h-0">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ) : (
                    <>
                        {response.length === 0 ? (
                            <div className="text-center text-text/70 text-lg py-8">Ni dogodkov za prikaz.</div>
                        ) : (
                            response.map((event) => {
                                return (
                                    <EventForList
                                        key={event.id}
                                        event={event}
                                    />
                                );
                            })
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default ListAllEvents;