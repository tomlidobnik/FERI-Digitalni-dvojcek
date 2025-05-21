import { Link } from "react-router-dom";

const EventForListDetail = ({event}) => {
    return (
    <div className="flex flex-col bg-white/30 rounded-2xl shadow-xl mb-4 p-4">
        <div className="flex backdrop-blur-md rounded-2xl items-center justify-center">
            <div className="flex-1 text-left text-xl xl:text-4xl font-semibold whitespace-nowrap text-text overflow-hidden text-ellipsis">
                {event.title}
            </div>
                <Link
                    to="/event"
                    className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-4 px-6 xl:px-8 flex items-center justify-center hover:bg-quaternary hover:text-white transition group"
                >
                    
                    <img
                        src="icons/angle-double-right.svg"
                        className="w-6 h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:scale-125"
                        alt="details"
                    />
                </Link>

        </div>
        <div className="flex p-2 text-lg">
                {event.start_date} - {event.end_date}
        </div>
        <div className="flex p-2 text-lg">
                {event.description}
        </div>
    </div>
    );
};

export default EventForListDetail;