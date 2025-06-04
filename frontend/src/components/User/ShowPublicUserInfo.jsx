import { useState } from 'react';
import Notification from '../Notification';

const ShowPublicUserInfo = ({ userData }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [user, setUser] = useState(userData);

    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });

    return (
        <div className="flex flex-col items-center gap-6 align-center p-6">
            <Notification
                message={notification.message}
                type={notification.type}
                onClose={() => setNotification({ message: null, type: "info" })}
            />
            <div className="w-24 h-24 rounded-full bg-quaternary flex items-center justify-center text-white text-4xl font-bold shadow-lg">
                {user.username ? user.username.charAt(0).toUpperCase() : "U"}
            </div>
            <div className="w-full max-w-md bg-white/40 rounded-xl shadow p-6 flex gap-y-3 flex-col gap-3">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full py-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ) : (
                    <>
                        <div className="flex gap-4 items-start">
                            <div className="flex-2/3">
                                <span className="block text-gray-500 text-xs">Uporabniško ime</span>
                                <span className="text-base text-black">{user.username}</span>
                            </div>
                        </div>
                        <div className="flex gap-4">
                            <div className="flex-1">
                                <span className="block text-gray-500 text-xs">Ime</span>
                                <span className="text-base text-black">{user.first_name}</span>
                            </div>
                            <div className="flex-1">
                                <span className="block text-gray-500 text-xs">Priimek</span>
                                <span className="text-base text-black">{user.last_name}</span>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};

export default ShowPublicUserInfo;