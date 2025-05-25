import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import CreateEventForm from "./CreateEventForm";

const Event = () => {
    return (
        <div className="flex flex-col h-full md:rounded-2xl md:shadow-2xl md:bg-primary overflow-y-auto">
                <CreateEventForm />
        </div>
    );
};

export default Event;