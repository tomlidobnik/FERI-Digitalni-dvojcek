import { Link } from "react-router-dom";

const EventForList = ({event}) => {
    return (
    <div className="flex justify-between bg-white/30 backdrop-blur-md rounded-2xl shadow-xl mb-4">
        <div className="flex-1 text-left text-xl xl:text-2xl font-semibold whitespace-nowrap text-text p-4 overflow-hidden text-ellipsis">
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
    );
};

export default EventForList;