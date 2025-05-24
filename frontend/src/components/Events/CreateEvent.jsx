import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import ListAllEventsDetail from "./ListAllEventsDetail";
import CreateEventForm from "./CreateEventForm";

const Event = () => {
    return (
        <div className="flex flex-col h-full md:rounded-2xl shadow-2xl bg-primary p-4 xl:p-8">
            <CreateEventForm />
        </div>
    );
};

export default Event;