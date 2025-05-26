import { useState, useEffect } from "react";
import EventForListDetail from "./EventForListDetail";
import { Link } from "react-router-dom";
import Cookies from 'js-cookie';

const ListAllEventsDetail = ({selectMode}) => {
    const API_URL = import.meta.env.VITE_API_URL;
    const [isLoading, setIsLoading] = useState(true);
    const [response, setResponse] = useState([]);
    const [filteredResponse, setFilteredResponse] = useState([]);
    const [dogodkiTitle, setDogodkiTitle] = useState("Dogodki");

    // Fetch events when selectMode or API_URL changes
    useEffect(() => {
        setIsLoading(true);
        setDogodkiTitle(
            ["Vsi dogodki", "Trenutni dogodki", "Prihajajoči dogodki", "Pretekli dogodki", "Moji dogodki"][selectMode] || "Dogodki"
        );
        const token = Cookies.get("token");
        const url = selectMode === 4
            ? `https://${API_URL}/api/event/my`
            : `https://${API_URL}/api/event/all`;
        const headers = selectMode === 4 && token
            ? { Authorization: `Bearer ${token}` }
            : {};

        fetch(url, { headers })
            .then(res => res.json())
            .then(data => {
                setResponse(data.reverse());
            })
            .catch(err => {
                console.error(err);
                setResponse([]);
            });
    }, [API_URL, selectMode]);

    // Filter events and handle loading state
    useEffect(() => {
        setIsLoading(true);
        const now = new Date();
        const filtered = response.filter(event => {
            const start = new Date(event.start_date);
            const end = new Date(event.end_date);
            if (selectMode === 0) return true;
            if (selectMode === 1) return start <= now && end >= now;
            if (selectMode === 2) return start > now;
            if (selectMode === 3) return end < now;
            if (selectMode === 4) return true;
            return true;
        });
        setFilteredResponse(filtered);
        setTimeout(() => setIsLoading(false), 300);
    }, [response, selectMode]);

    return (
        <div className="w-full flex flex-col h-full">
            <h1 className="text-2xl p-4 xl:p-6 xl:text-5xl font-bold text-text bg-black/10 md:rounded-t-2xl flex-shrink-0">
                <span className="hidden md:inline">Dogodki</span>
                <span className="md:hidden">{dogodkiTitle}</span>
            </h1>
            <div className="flex-1 overflow-y-auto p-2 sm:p-4">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ) : filteredResponse.length < 1 ? (
                    <div className="text-center text-text/70 text-lg py-8">Ni dogodkov za prikaz.</div>
                ) : (
                    filteredResponse.map((event) => (
                        <EventForListDetail
                            key={event.id}
                            event={event}
                            selectMode={selectMode}
                        />
                    ))
                )}
            </div>
        </div>
    );
};

export default ListAllEventsDetail;