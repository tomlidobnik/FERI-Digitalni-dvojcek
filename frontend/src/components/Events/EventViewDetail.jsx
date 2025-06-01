const EventViewDetail = ({ event }) => {
    return (
        <div className="flex flex-col gap-4 p-4">
            <h2 className="text-2xl font-bold">{event.title}</h2>
            <p className="text-gray-700">{event.description}</p>
            <div className="flex items-center gap-4">
                <span className="text-sm text-gray-500">Datum: {new Date(event.date).toLocaleDateString()}</span>
                <span className="text-sm text-gray-500">Čas: {new Date(event.date).toLocaleTimeString()}</span>
            </div>
            <div className="flex items-center gap-4">
                <span className="text-sm text-gray-500">Lokacija: {event.location?.name || "Neznana lokacija"}</span>
            </div>
        </div>
    );
}

export default EventViewDetail;