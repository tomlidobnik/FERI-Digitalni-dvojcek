import { use, useEffect, useState} from "react";
import Cookies from "js-cookie";
import Notification from "../Notification";
import ShowPublicUserInfo from "./ShowPublicUserInfo";
import { useParams } from "react-router-dom";

const PublicProfile = () => {
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const API_URL = import.meta.env.VITE_API_URL;
    const token = Cookies.get("token");
    const { id } = useParams();
    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });

    useEffect(() => {
        if (!token) {
            setNotification({
                message: `Napaka pri pridobivanju tokena.`,
                type: "erro",
            });
        }

        setIsLoading(true);
        fetch(`https://${API_URL}/api/user/by_id/${id}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then((res) => res.json())
            .then((data) => {
                setUser(data);
                setIsLoading(false);
            })
            .catch((err) => {
                setNotification({
                    message: `Napaka pri pridobivanju uporabnika.`,
                    type: "erroe",
                });
                setIsLoading(false);
            });
    }, []);

    return (
        <div className="flex flex-col bg-primary rounded-2xl h-full">
            <Notification
                message={notification.message}
                type={notification.type}
                onClose={() => setNotification({ message: null, type: "info" })}
            />
            {isLoading ? (
                <div className="flex items-center justify-center h-full pt-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                </div>
            ) : (
                user && (
                    <ShowPublicUserInfo userData={user}/>
                )
            )}
        </div>
    );
}
export default PublicProfile;