import { useState } from "react";
import EventForList from "./EventForList";

const ListAllEvents = () => {
    const [response, setResponse] = useState(null);

    const handlePost = async () => {
        try {
            const res = await fetch("/api/events", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ name: "New Event" }),
            });
            const data = await res.json();
            setResponse(data);
        } catch (error) {
            setResponse({ error: "Failed to post event" });
        }
    };

    return (
        <div>

        </div>
    );
};

export default ListAllEvents;