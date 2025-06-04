import { useEffect, useState, useRef } from "react";

const Notification = ({ message, type = "info", duration = 6000, onClose }) => {
    const [visible, setVisible] = useState(false);
    const autoCloseTimerRef = useRef(null);

    useEffect(() => {
        if (message) {
            setVisible(true);
            if (autoCloseTimerRef.current) {
                clearTimeout(autoCloseTimerRef.current);
            }
            autoCloseTimerRef.current = setTimeout(() => {
                setVisible(false);
                setTimeout(onClose, 300);
            }, duration);
        } else {
            setVisible(false);
            if (autoCloseTimerRef.current) {
                clearTimeout(autoCloseTimerRef.current);
            }
        }
        return () => {
            if (autoCloseTimerRef.current) {
                clearTimeout(autoCloseTimerRef.current);
            }
        };
    }, [message, duration, onClose]);

    const handleManualClose = () => {
        if (autoCloseTimerRef.current) {
            clearTimeout(autoCloseTimerRef.current);
        }
        setVisible(false);
        setTimeout(onClose, 300);
    };

    if (!message && !visible) return null;

    const baseStyle =
        "fixed bottom-5 right-5 p-2 pr-10 rounded-lg shadow-xl text-text-custom transition-all duration-500 ease-in-out z-[3000] border-black/20 border-4";
    const typeStyles = {
        info: "bg-primary",
        success: "bg-tertiary",
        error: "bg-error",
        warning: "bg-warning",
    };

    const notificationStyle = `
    ${baseStyle}
    ${typeStyles[type] || typeStyles.info}
    ${
        visible
            ? "opacity-100 translate-x-0"
            : "opacity-0 translate-x-full pointer-events-none"
    }
  `;

    return (
        <div className={notificationStyle}>
            {message}
            <button
                onClick={handleManualClose}
                className="absolute top-1/2 right-3 transform -translate-y-1/2 text-text-custom hover:text-white text-2xl font-light leading-none"
                aria-label="Close notification">
                &times;
            </button>
        </div>
    );
};

export default Notification;
