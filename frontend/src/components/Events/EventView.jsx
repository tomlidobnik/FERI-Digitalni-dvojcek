import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import CreateEventForm from "./CreateEventForm";
import { useParams } from "react-router-dom";
import Notification from "../Notification";
import Cookies from "js-cookie";

import EventViewDetail from "./EventViewDetail";
import EventMap from "../Map/EventMap";


const EventView = () => {
    const API_URL = import.meta.env.VITE_API_URL;
    
    const { id } = useParams();
    const [event, setEvent] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        setIsLoading(true);
        const token = Cookies.get("token");
        fetch(`https://${API_URL}/api/event/by_id/${id}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then(res => res.json())
            .then(data => {setEvent(data); console.log("Fetched locations:", data);})
            .catch(err => {
                setNotification({
                    message: "Napaka med pridobivanjem dogodka.",
                    type: "error",
                });
                setAllLocations([]);
        });
        setIsLoading(false);
    },[API_URL, id]);

    return (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 min-h-screen">
            {/* Event Info - spans 2 columns on desktop */}
            <div className="col-span-1 md:col-span-2 bg-primary rounded-2xl shadow-xl min-h-[300px] h-full p-4">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ):(
                    <>{event != null? <EventViewDetail event={event}/> : null}</>
                )}
            </div>
            {/* Map */}
            <div className="col-span-1 bg-primary rounded-2xl shadow-xl min-h-[300px] h-full flex flex-col">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ):(
                    <>{event != null? <EventMap location_fk={event.location_fk} /> : null}</>
                )}
            </div>
            {/* Chat - full width on mobile, 2 columns on desktop */}
            <div className="col-span-1 md:col-span-3 bg-primary rounded-2xl shadow-xl min-h-[200px] h-full p-4">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ):(
                    <>{event != null? event.title : null}</>
                )}
            </div>
        </div>
    );
};

export default EventView;