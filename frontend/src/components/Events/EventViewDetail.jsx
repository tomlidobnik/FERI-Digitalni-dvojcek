import { formatDateTime } from "../../utils/formatDateTime";
import React from "react";
import {Link} from "react-router-dom";


const EventViewDetail = ({ event, owner, location }) => {
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
            { owner && (
            <Link to={`/profile/${owner.id}`} className="flex flex-row items-center gap-2 w-fit bg-blue-300 rounded-xl font-semibold p-3 shadow-md">
                    <img src="../icons/user-bold.svg" className="w-4 h-4" alt="location" />
                    {owner ? <> {owner.username ? owner.username : "User"} </> : "User"}
            </Link>)}
            <div className="">
                <div className="text-text bg-white/30 rounded-xl shadow-md p-4 w-full h-full text-lg font-semibold break-words">{event.description || "Brez opisa."}</div>
            </div>
        </div>
    );
};

export default EventViewDetail;