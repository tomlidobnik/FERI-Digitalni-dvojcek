import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import Cookies from 'js-cookie';
import LeftSidebar from "../components/Sidebar/LeftSidebar.jsx";
import Map from "./map/LocationsMap.jsx";
import Event from "../components/Events/Event.jsx";
import ListAllFriends from "../components/Friends/ListAllFriends.jsx";
import CreateEvent from "../components/Events/CreateEvent.jsx";
import EditEvent from "../components/Events/EditEvent.jsx";

function MainPage() {
    const navigate = useNavigate();
    const location = useLocation().pathname;

    useEffect(() => {
        const token = Cookies.get("token");
        if (!token) {
            navigate ("/login");
        }
    });

    return (
        <>
            <div className="flex flex-col lg:flex-row h-screen">
                <div className="lg:pr-0 lg:p-6 w-full lg:h-full lg:w-auto">
                    <LeftSidebar />
                </div>
                <div className="flex-1 ml-0 h-full overflow-y-auto">
                    <div className="flex flex-col h-full md:p-6">
                        {location === "/map" ? <Map />: <></>}
                        {location === "/events" ? <Event />: <></>}
                        {location === "/events/add" ? <CreateEvent />: <></>}
                        {location === "/event/edit" ? <EditEvent />: <></>}
                    </div>

                </div>
                <div className="text-lg bg-quaternary text-center py-4 lg:hidden block relative bottom-0 left-0 right-0">
                    {new Date().getFullYear()} Copycats
                </div>
            </div>
        </>

    );
}

export default MainPage;