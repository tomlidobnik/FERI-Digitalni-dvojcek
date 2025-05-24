import { Link } from "react-router-dom";

const formatDateTime = (dateString) => {
    const date = new Date(dateString);
    return `${date.getDate()}. ${date.getMonth() + 1}. ${date.getFullYear()} ob ${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
};

const EventForListDetail = ({event}) => {
    return (
    <div className="flex flex-col bg-white/30 rounded-2xl shadow-xl mb-4 p-4">
        <div className="flex backdrop-blur-md rounded-2xl items-center justify-center">
            <div className="flex-1 text-left text-xl xl:text-4xl font-semibold whitespace-nowrap text-text overflow-hidden text-ellipsis">
                {event.title}
            </div>
                <Link
                    to="/event"
                    className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-4 px-6 xl:px-8 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                >
                    <img
                        src="icons/angle-double-right.svg"
                        className="w-6 h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:scale-125"
                        alt="details"
                    />
                </Link>

        </div>
        <div className="flex flex-wrap gap-2 pt-2 px-2 sm:items-center text-base font-medium">
            <div className=" flex px-2 py-1 bg-secondary/30 rounded text-sm font-semibold w-fit">
                <img src="icons/hourglass-start.svg" className="w-5 h-5 mr-2" alt="location" />
                {formatDateTime(event.start_date)}
            </div>
            <div className="flex px-2 py-1 bg-secondary/30 rounded text-sm font-semibold w-fit">
                <img src="icons/hourglass-end.svg" className="w-5 h-5 mr-2" alt="location" />
                {formatDateTime(event.end_date)}
            </div>
        </div>
        <div className="flex p-2 pb-0 text-lg description-clamp">
                {event.description}
        </div>
    </div>
    );
};

export default EventForListDetail;