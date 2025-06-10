import { useState, useEffect } from "react";
import Notification from "../Notification";

const LocationNameModal = ({ isOpen, onSave, onClose }) => {
    const [locationName, setLocationName] = useState("");
    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });
    const [showContent, setShowContent] = useState(false);

    useEffect(() => {
        if (isOpen) {
            const timer = setTimeout(() => setShowContent(true), 50);
            return () => clearTimeout(timer);
        } else {
            setShowContent(false);
            setLocationName("");
        }
    }, [isOpen]);

    const handleSave = () => {
        if (locationName.trim() === "") {
            setNotification({
                message: "Prosimo, vnesite ime za lokacijo.",
                type: "warning",
            });
            return;
        }
        onSave(locationName);
    };

    if (!isOpen && !showContent) {
        return null;
    }

    return (
        <div
            className={`absolute inset-0 bg-black/45 flex items-center justify-center z-[2000] transition-opacity duration-300 ease-in-out ${
                isOpen ? "opacity-100" : "opacity-0 pointer-events-none"
            }`}>
            <Notification
                message={notification.message}
                type={notification.type}
                onClose={() => setNotification({ message: null, type: "info" })}
            />
            <div
                className={`bg-primary p-6 rounded-lg shadow-xl w-full max-w-md mx-4 text-text-custom transform transition-all duration-300 ease-in-out ${
                    showContent && isOpen
                        ? "opacity-100 scale-100"
                        : "opacity-0 scale-95"
                }`}>
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-semibold">
                        Vnesi ime lokacije
                    </h3>
                    <button
                        onClick={onClose}
                        className="text-text-custom hover:text-text-hover text-2xl font-bold"
                        aria-label="Zapri modalno okno">
                        &times;
                    </button>
                </div>
                <input
                    type="text"
                    value={locationName}
                    onChange={(e) => setLocationName(e.target.value)}
                    placeholder="npr. igrišče, park"
                    className="border border-quaternary/50 p-3 rounded-2xl w-full mb-4 bg-primary/30 text-text-custom placeholder-text-custom/70 focus:ring-tertiary focus:border-tertiary"
                    autoFocus
                />
                <div className="flex justify-end space-x-2">
                    <button
                        onClick={handleSave}
                        className="bg-tertiary hover:brightness-85 text-text-custom font-bold py-2 px-4 rounded-xl transition-all duration-300 ease-in-out">
                        Shrani
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LocationNameModal;
