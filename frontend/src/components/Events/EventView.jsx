import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import CreateEventForm from "./CreateEventForm";
import { useParams } from "react-router-dom";
import Notification from "../Notification";
import Cookies from "js-cookie";

import EventViewDetail from "./EventViewDetail";
import EventMap from "../Map/EventMap";
import EventChat from "../Chat/EventChat";

const EventView = () => {
    const API_URL = import.meta.env.VITE_API_URL;
    
    const { id } = useParams();
    const [event, setEvent] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [owner, setOwner] = useState(null);
    const [location, setLocation] = useState(null);
    const [outline, setOutline] = useState(null);

    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });

    useEffect(() => {
        setIsLoading(true);
        const token = Cookies.get("token");
        fetch(`https://${API_URL}/api/event/by_id/${id}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then(res => res.json())
            .then(data => {
                setEvent(data);
                fetch(`https://${API_URL}/api/user/by_id/${data.user_fk}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                })
                    .then(res => res.json())
                    .then(data => {setOwner(data);})
                    .catch(err => {
                        setNotification({
                            message: "Napaka med pridobivanjem dogodka.",
                            type: "error",
                        });
                    });
                fetch(`https://${API_URL}/api/location/by_id/${data.location_fk}`)
                    .then(res => res.json())
                    .then(data => {
                        setLocation(data);
                        // If location_outline_fk exists, fetch the outline
                        if (data.location_outline_fk) {
                            fetch(`https://${API_URL}/api/location_outline/by_id/${data.location_outline_fk}`)
                                .then(res => res.json())
                                .then(outlineData => {setOutline(outlineData); console.log(outlineData);})
                                .catch(() => setOutline(null));
                        } else {
                            setOutline(null);
                        }
                    })
                    .catch(err => setLocation(null));
                    })
            .catch(err => {
                setNotification({
                    message: "Napaka med pridobivanjem dogodka.",
                    type: "error",
                });
        });

        setIsLoading(false);
    },[API_URL, id]);

    return (
        <div className="grid grid-cols-1 md:grid-cols-3 md:gap-6 min-h-full">
            {/* Event Info - spans 2 columns on desktop */}
            <div className="col-span-1 md:col-span-2 bg-primary md:rounded-2xl shadow-xl min-h-[300px] h-full p-4">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ):(
                    <>{event != null? <EventViewDetail event={event} owner={owner} location={location}/> : null}</>
                )}
            </div>
            {/* Map */}
            <div className="col-span-1 bg-primary md:rounded-2xl shadow-xl min-h-[50vh] h-full flex flex-col">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ):(
                    <>{location != null ? <EventMap location={location} outline={outline}/> : <div className="flex items-center justify-center h-full w-full text-center">Ni lokacije</div>}</>
                )}
            </div>
            {/* Chat - full width on mobile, 2 columns on desktop */}
            <div className="col-span-1 md:col-span-3 bg-primary md:rounded-2xl shadow-xl min-h-[300px] h-[44vh] p-4">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ):(
                    <EventChat eventId={id}/>
                )}
            </div>
        </div>
    );
};

export default EventView;