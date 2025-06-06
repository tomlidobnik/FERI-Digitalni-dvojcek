import { useState } from 'react';
import { useForm } from "react-hook-form";
import Notification from '../Notification';
import Cookies from 'js-cookie';

const ShowUserInfo = ({ userData }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const API_URL = import.meta.env.VITE_API_URL;
    const [user, setUser] = useState(userData);
    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });

    function setUserToEdit() {
        setIsEditing(true);
    }

    function setUserNotEdit() {
        setIsEditing(false);
        reset();
    }

    const { 
        register,
        handleSubmit,
        setError,
        formState: { errors },
        reset,
    } = useForm();

    const refreshUserData = async () => {
        try {
            const response = await fetch(`https://${API_URL}/api/user/by_token`, {
                headers: {
                    "Authorization": `Bearer ${Cookies.get("token")}`,
                },
            });
            if (!response.ok) {
                const errorData = await response.json();
                setNotification({
                    message: "Napaka pri osveževanju uporabniških podatkov." + errorData.error,
                    type: "error",
                });
                return;
            }
            const data = await response.json();
            setUser(data);
            return data;
        } catch (error) {
            setNotification({
                message: "Napaka pri osveževanju uporabniških podatkov: " + error.message,
                type: "error",
            });
            return null;
        }
    }

    const onSubmit = async (data) => {
        try {
            setIsLoading(true);
            const userData = {
                username: data.username,
                firstname: data.firstname,
                lastname: data.lastname,
                email: data.email,
            }

            const userForTokenData = {
                username: data.username,
                password: data.password,
            }

            reset({ password: "" });

            console.log("User data to update:", userData);
            const response = await fetch(`https://${import.meta.env.VITE_API_URL}/api/user/update`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${Cookies.get("token")}`,
                },
                body: JSON.stringify(userData),
            });
            if (!response.ok) {
                const errorData = await response.json();
                setNotification({
                    message: "Uporabniški podatki niso bili posodobljeni:" + errorData.error,
                    type: "warning",
                });
                setIsEditing(false);
                setIsLoading(false);
                return;
            } else {
                const response = await fetch(`https://${import.meta.env.VITE_API_URL}/api/user/token`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(userForTokenData),
                });
                if (!response.ok) {
                    const errorData = await response.json();
                    if (errorData.error) {
                        setNotification({
                            message: "Napaka pri pridobivanju JWT.",
                            type: "Error",
                        });
                        setIsLoading(false);
                        return;
                    }
                }else if (response.ok) {
                    const token = await response.text();
                    const expirationDate = new Date(Date.now() + 30 * 60 * 1000);
                    Cookies.set("token", token, { expires: expirationDate, sameSite: "strict" });
                    setNotification({
                        message: "Uporabniški podatki so bili uspešno posodobljeni.",
                        type: "success",
                    });
                    await refreshUserData();
                    Cookies.set("user", JSON.stringify(data), { expires: expirationDate, sameSite: "strict" });
                    setIsEditing(false);
                    setIsLoading(false);
                }
            }
        } catch (error) {
            setNotification({
                message: "Napaka pri posodabljanju uporabniških podatkov." + error.message,
                type: "error",
            });
            setIsLoading(false);
        }
    }

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
                <div className="w-full max-w-md bg-white/40 rounded-xl shadow p-6 flex gap-y-3 flex-col gap- gap-3">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full py-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ): (
                    <>
                    {isEditing ? (
                        <form onSubmit={handleSubmit(onSubmit)}>
                            <div className="flex gap-4 items-start">
                                <div className="flex-2/3">
                                    <span className="block text-gray-500 text-xs">Uporabniško ime</span>
                                    <input {...register("username", {
                                        required: "Uporabniško ime je obvezno",
                                    })}
                                        type="text"
                                        defaultValue={user.username}
                                        className="bg-black/10 p-2 text-base rounded-lg shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 w-full"
                                        {...register("username")}
                                    />
                                    <div className="text-error h-2">
                                        {errors.username && <>{errors.username.message}</>}
                                    </div>
                                </div>
                                <div className="relative flex flex-row gap-2 flex-shrink-0 ml-auto mb-auto">
                                    <button
                                        type="submit"
                                        className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                                    >
                                        <img
                                            src="icons/check.svg"
                                            className="w-4 h-4 transition-transform duration-300 group-hover:scale-125"
                                            alt="edit"
                                        />
                                    </button>
                                    <button 
                                        type="button"
                                        onClick={setUserNotEdit}
                                        className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                                    >
                                        <img
                                            src="icons/cross.svg"
                                            className="w-4 h-4 transition-transform duration-300 group-hover:scale-125"
                                            alt="edit"
                                        />
                                    </button>
                                </div>
                            </div>
                            <div className="flex gap-4 py-5">
                                <div className="flex-1">
                                    <span className="block text-gray-500 text-xs">Ime</span>
                                    <input {...register("firstname", {
                                        required: "Ime je obvezno",
                                    })}
                                        type="text"
                                        defaultValue={user.first_name}
                                        className="bg-black/10 p-2 text-base rounded-lg shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 w-full"
                                    />
                                    <div className="text-error h-2">
                                        {errors.firstname && <>{errors.firstname.message}</>}
                                    </div>
                                </div>
                                <div className="flex-1">
                                    <span className="block text-gray-500 text-xs">Priimek</span>
                                    <input {...register("lastname",{
                                        required: "Priimek je obvezno",
                                    })}
                                        type="text"
                                        defaultValue={user.last_name}
                                        className="bg-black/10 p-2 text-base rounded-lg shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 w-full"
                                    />
                                    <div className="text-error h-2">
                                        {errors.lastname && <>{errors.lastname.message}</>}
                                    </div>
                                </div>
                            </div>
                            <div className='pb-4'>
                                <span className="block text-gray-500 text-xs">E-naslov</span>
                                <input
                                    {...register("email", {
                                        required: "E-naslov je obvezen",
                                        pattern: {
                                            value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                                            message: "E-naslov ni veljaven"
                                        }
                                    })}
                                    type="text"
                                    defaultValue={user.email}
                                    className="bg-black/10 p-2 text-base rounded-lg shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 w-full"
                                />
                                <div className="text-error h-2">
                                    {errors.email && <>{errors.email.message}</>}
                                </div>
                            </div>
                            <div className='pb-4'>
                                <span className="block text-gray-500 text-xs">Geslo</span>
                                <input {...register("password",{
                                    required: "Geslo je obvezno",
                                })} type="password"                      
                                    className="bg-black/10 p-2 text-base rounded-lg shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 w-full"/>
                                <div className="text-error h-2">
                                    {errors.password && <>{errors.password.message}</>}
                                </div>
                            </div>
                        </form>
                    ) : (
                        <>
                            <div className="flex gap-4 items-start">
                                <div className="flex-2/3">
                                    <span className="block text-gray-500 text-xs">Uporabniško ime</span>
                                    <span className="text-base text-black">{user.username}</span>
                                </div>
                                <div className="relative flex-shrink-0 ml-auto mb-auto">
                                    <button
                                        type="button"
                                        onClick={setUserToEdit}
                                        className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                                    >
                                        <img
                                            src="icons/pencil.svg"
                                            className="w-4 h-4 transition-transform duration-300 group-hover:scale-125"
                                            alt="edit"
                                        />
                                    </button>
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
                            <div>
                                <span className="block text-gray-500 text-xs">E-naslov</span>
                                <span className="text-base text-black">{user.email}</span>
                            </div>
                        </>
                    )}</>)}
                </div>
            </div>
    );
};

export default ShowUserInfo;