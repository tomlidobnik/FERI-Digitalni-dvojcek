import { Link } from "react-router-dom";
import ConfirmPopup from "./ConfirmPopup";
import { useState } from "react";
import Cookies from "js-cookie";
import { useSelector } from "react-redux";
import { setEvent } from "../../state/event/eventSlice";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

const formatDateTime = (dateString) => {
    const currentDate = new Date();
    const date = new Date(dateString);
    if (currentDate.getDay() === date.getDay() &&
        currentDate.getMonth() === date.getMonth() &&
        currentDate.getFullYear() === date.getFullYear()) {
        return `Danes ob ${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
    };
    return `${date.getDate()}. ${date.getMonth() + 1}. ${date.getFullYear()} ob ${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
};

const EventForListDetail = ({event, selectMode, location}) => {
    const [showConfirm, setShowConfirm] = useState(false);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleDelete = async (id) => {
        try {
            const token = Cookies.get("token");
            const response = await fetch(`https://${import.meta.env.VITE_API_URL}/api/event/delete/${id}`, {
                method: "DELETE",
                headers: {
                    Authorization: token ? `Bearer ${token}` : "",
                },
            });
            if (response.ok) {
                console.log("Event deleted!");
                window.location.reload();
            } else {
                console.error("Failed to delete event");
            }
        } catch (err) {
            console.error("Error deleting event:", err);
        }
        setShowConfirm(false);
    };

    function setEventToEdit () {
        dispatch(setEvent(event));
        navigate("/event/edit");
    }

    return (
    <div className="flex flex-col bg-white/30 rounded-2xl shadow-md mb-4 p-4">
        <ConfirmPopup
                open={showConfirm}
                message="Ali res želiš izbrisati ta dogodek?"
                onConfirm={() => handleDelete(event.id)}
                onCancel={() => setShowConfirm(false)}
            />
        <div className="flex backdrop-blur-md rounded-2xl items-center justify-center">
            <div className="flex-1 text-left text-xl xl:text-4xl font-semibold whitespace-nowrap text-text overflow-hidden text-ellipsis">
                {event.title}
            </div>
            <div className="flex flex-row">
                {selectMode === 4 ? (
                    <>
                        <button
                            onClick={() => setShowConfirm(true)}
                            className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 md:p-4 md:px-6 xl:px-8 mr-4 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                        >
                            <img
                                src="icons/trash.svg"
                                className="w-4 h-4 md:w-6 md:h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:scale-125"
                                alt="delete"
                            />
                        </button>
                        <button
                            onClick={() => setEventToEdit()}
                            className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 md:p-4 md:px-6 xl:px-8 mr-4 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                        >
                            <img
                                src="icons/pencil.svg"
                                className="w-4 h-4 md:w-6 md:h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:scale-125"
                                alt="edit"
                            />
                        </button>
                    </>
                ): null}
                <Link
                    to="/event"
                    className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 md:p-4 md:px-6 xl:px-8 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                >
                    <img
                        src="icons/angle-double-right.svg"
                        className="w-4 h-4 md:w-6 md:h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:scale-125"
                        alt="details"
                    />
                </Link>
            </div>
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
            {location ? (
                <div className="flex px-2 py-1 bg-tertiary/30 rounded text-sm font-semibold w-fit">
                    <img src="icons/location-marker.svg" className="w-5 h-5 mr-2" alt="location" />
                    {location ? (location.name || location.info || "Neznana lokacija") : "Neznana lokacija"}
                </div>
            ):""}

        </div>
        <div className="flex p-2 pb-0 text-lg description-clamp">
                {event.description}
        </div>
    </div>
    );
};

export default EventForListDetail;