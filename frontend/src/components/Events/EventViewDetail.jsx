import { formatDateTime } from "../../utils/formatDateTime";
import React, { use } from "react";
import {Link} from "react-router-dom";
import Tag from "./Tag";
import Cookies from "js-cookie";
import { useState, useEffect } from "react";

const EventViewDetail = ({ event, owner, location}) => {
    const [attending, setAttending] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [token, setToken] = useState(Cookies.get("token"));
    const [update, setUpdate] = useState(false);

    useEffect(() => {
        try{
            async function fetchAttending() {
                try {
                    const res = await fetch(`https://${import.meta.env.VITE_API_URL}/api/event/get_users/${event.id}/`);
                    if (res.ok) {
                        const data = await res.json();
                        setAttending(data);
                    } else {
                        setAttending([]);
                    }
                } catch {
                    setAttending([]);
                }
            }
            if (event?.id) {
                fetchAttending();
            }
        }catch (error) {
            console.error("Error fetching attending users:", error);
            setAttending([]);
        }
    }, [event?.id, update]);

    async function attendEvent(){
        console.log("Attending event:", event.id);
        if (attending.includes(JSON.parse(Cookies.get("user")).username)) {
            const response = await fetch(`https://${import.meta.env.VITE_API_URL}/api/event/leave_event/${event.id}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: token ? `Bearer ${Cookies.get("token")}` : "",
                    },
                }
            );
            if (response.ok) {
                console.log("Successfully attended event");
                setUpdate(!update);
            } else {
                console.error("Failed to attend event");
            }
        }else{
            const response = await fetch(`https://${import.meta.env.VITE_API_URL}/api/event/join_public_event/${event.id}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: token ? `Bearer ${token}` : "",
                    },
                }
            );
            if (response.ok) {
                console.log("Successfully attended event");
                setUpdate(!update);
            } else {
                console.error("Failed to attend event");
            }
        }
    }

    return (
        <div className="flex flex-col gap-3 p-4 rounded-2xl">
            <h1 className="text-2xl lg:text-3xl xl:text-4xl font-bold break-words">{event.title}</h1>
            <div className="flex flex-col md:flex-row md:items-center gap-2">
                <span className="flex flex-row w-fit items-center gap-2 md:mr-2 mr-0 bg-secondary/30 rounded-xl font-semibold p-3 shadow-md">
                    <img src="../icons/hourglass-start.svg" className="w-5 h-5" alt="location" />
                    {formatDateTime(event.start_date)}
                </span>
                <span className="flex flex-row w-fit items-center gap-2 bg-secondary/30 rounded-xl font-semibold p-3 shadow-md">
                    <img src="../icons/hourglass-end.svg" className="w-5 h-5" alt="location" />
                    {formatDateTime(event.end_date)}
                </span>
            </div>
            { location && (
                <div className="flex items-center">
                <span className="flex flex-row items-center gap-2 bg-tertiary/30 rounded-xl font-semibold p-3 shadow-md">
                        <img src="../icons/location-marker.svg" className="w-4 h-4" alt="location" />
                        {location.name || location.info || "Neznana lokacija"}
                    </span>
                </div>
            )}
            {event.tag ? (                    
                <div className="w-fit">
                    <Tag tag={event.tag} specialCssImage={"w-4 h-4 mr-2"} specialCssText={"p-3 rounded-xl shadow-md font-semibold"}/>
                </div>): null
            }
            { owner && (
            <Link to={`/profile/${owner.id}`} className="flex flex-row items-center gap-2 w-fit bg-blue-300 rounded-xl font-semibold p-3 shadow-md">
                    <img src="../icons/user-bold.svg" className="w-4 h-4" alt="location" />
                    {owner ? <> {owner.username ? owner.username : "User"} </> : "User"}
            </Link>)}
            <div className="flex flex-col md:flex-row md:items-center gap-2">
                <span className="relative group flex flex-row w-fit items-center gap-2 md:mr-2 mr-0 bg-blue-300 rounded-xl font-semibold p-3 shadow-md hover:bg-blue-400">
                    <img src="../icons/users-alt.svg" className="w-5 h-5" alt="location" />
                    {attending.length}
                    {/* Tooltip with usernames */}
                    <div className="absolute left-1/2 top-full z-20 mt-2 w-max min-w-[120px] max-w-[220px] bg-primary text-black text-sm  border-2 rounded-xl shadow-lg p-3 opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity duration-200 -translate-x-1/2">
                        {attending.length === 0
                        ? <span>Nihče še ne prihaja</span>
                        : (
                            <ul>
                            {attending.map((u, i) => (
                                <li key={i}>{u}</li>
                            ))}
                            </ul>
                        )
                        }
                    </div>
                </span>
                <span
                className={`flex flex-row w-fit items-center gap-2 rounded-xl font-semibold p-3 shadow-md cursor-pointer transition-all duration-200 ${
                    attending.includes(JSON.parse(Cookies.get("user")).username)
                      ? "bg-green-300 hover:bg-green-400"
                      : "bg-gray-300 hover:bg-blue-400"
                  }`}
                onClick={attendEvent}
                >
                <img src="../icons/check.svg" className="w-5 h-5" alt="location" />
                Pridem
                </span>
            </div>
            <div className="">
                <div className="text-text bg-white/30 rounded-xl shadow-md p-4 w-full h-full text-lg font-semibold break-words">{event.description || "Brez opisa."}</div>
            </div>
        </div>
    );
};

export default EventViewDetail;