import { Link } from "react-router-dom";
import { formatDateTime } from "../../utils/formatDateTime";

const EventForList = ({ event }) => {
    return (
        <div className="flex flex-row justify-between bg-white/30 rounded-2xl shadow-md p-2 mb-4 items-center">
            <div className="flex flex-col flex-1 min-w-0">
                <div className="text-left text-xl xl:text-2xl font-semibold text-text truncate">
                    {event.title}
                </div>
                <div className="flex px-2 bg-secondary/30 rounded text-sm font-semibold w-fit">
                    {formatDateTime(event.start_date)}
                </div>
                    <div className="flex items-center px-2 mt-1 bg-primary/20 rounded text-xs font-semibold w-fit">
                        #{event.tag}
                    </div>
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
