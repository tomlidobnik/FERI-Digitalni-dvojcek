import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import ListAllEventsDetail from "./ListAllEventsDetail";

const Event = () => {
    return (
        <div className="flex flex-col h-full md:rounded-2xl shadow-2xl bg-primary">
            <div className="flex">
                Adding Event
            </div>
        </div>
    );
};

export default Event;