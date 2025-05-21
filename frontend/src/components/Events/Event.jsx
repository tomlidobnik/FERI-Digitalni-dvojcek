import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import ListAllEventsDetail from "./ListAllEventsDetail";

const Event = () => {
    return (
        <div className="flex flex-col h-full md:rounded-2xl shadow-2xl bg-primary">
            <div className="flex h-5/6">
                <ListAllEventsDetail />
            </div>
            
            <div className="flex h-1/6 min-h-fit bg-black/5 md:rounded-b-2xl p-4 xl:p-8">
                <Link to="/events/add"className="btn-nav">
                    <div className="text-2xl">
                        Dodaj dogodek
                    </div>
                </Link>
            </div>
        </div>
    );
};

export default Event;