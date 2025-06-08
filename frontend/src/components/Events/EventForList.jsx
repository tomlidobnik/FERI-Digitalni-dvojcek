import { Link } from "react-router-dom";
import { formatDateTime } from "../../utils/formatDateTime";
import Tag from "./Tag"
import { useState, useEffect } from "react";

const EventForList = ({ event }) => {
    const [attending, setAttending] = useState([]);

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
            }, [event?.id]);
    return (
        <div className="flex flex-row justify-between bg-white/30 rounded-2xl shadow-md p-2 mb-4 items-center">
            <div className="flex flex-col flex-1 min-w-0">
                <div className="text-left text-xl xl:text-2xl font-semibold text-text truncate">
                    {event.title}
                </div>
                <div className="flex px-2 bg-secondary/30 rounded text-sm font-semibold w-fit">
                    {formatDateTime(event.start_date)}
                </div>
                {event.tag ? (                    
                    <div className="w-fit text-sm">
                        <Tag tag={event.tag} specialCssImage={"w-4 h-4 mr-1"} specialCssText={"mt-1 px-2"}/>
                    </div>): null
                }
                {event.location ? (
                    <div className="flex items-center px-2 mt-1 bg-tertiary/30 rounded text-sm font-semibold w-fit">
                        <img
                            src="icons/location-marker.svg"
                            className="w-4 h-4 mr-1"
                            alt="location"
                        />
                        {event.location
                            ? event.location.name ||
                              event.location.info ||
                              "Neznana lokacija"
                            : "Neznana lokacija"}
                    </div>
                ) : null}
                {attending?(                
                    <span className="flex items-center px-2 mt-1 rounded text-sm font-semibold w-fit  bg-blue-300 ">
                        <img src="../icons/users-alt.svg" className="w-4 h-4 mr-1" alt="location" />
                        {attending.length}
                    </span>)
                    :""
                }
            </div>
            <div className="flex flex-wrap sm:items-center text-base font-medium ml-2">
                <Link
                    to={`/event/${event.id}`}
                    className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 md:p-4 md:px-6 xl:px-8 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group">
                    <img
                        src="icons/angle-double-right.svg"
                        className="w-4 h-4 md:w-6 md:h-6 xl:w-6 xl:h-6 transition-transform duration-300 group-hover:scale-125"
                        alt="details"
                    />
                </Link>
            </div>
        </div>
    );
};

export default EventForList;
