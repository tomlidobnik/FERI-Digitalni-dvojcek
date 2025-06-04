import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import Cookies from 'js-cookie';
import LeftSidebar from "../components/Sidebar/LeftSidebar.jsx";
import Map from "./map/LocationsMap.jsx";
import Event from "../components/Events/Event.jsx";
import ListAllFriends from "../components/Friends/ListAllFriends.jsx";
import CreateEvent from "../components/Events/CreateEvent.jsx";
import EditEvent from "../components/Events/EditEvent.jsx";
import EventView from "../components/Events/EventView.jsx";
import Profile from "../components/User/Profile.jsx";

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
                <div className="lg:pr-0 lg:p-6 w-full lg:h-full lg:w-auto z-50">
                    <LeftSidebar />
                </div>
                <div className="flex-1 ml-0 h-full overflow-y-auto">
                    <div className="flex flex-col h-full md:p-6">
                        {location === "/map" ? <Map />: null}
                        {location === "/events" ? <Event />: null}
                        {location === "/events/add" ? <CreateEvent />: null}
                        {location === "/event/edit" ? <EditEvent />: null}
                        {/^\/event\/\d+$/.test(location) ? <EventView/>: null}
                        {location === "/profile" ? <Profile />: null}
                    </div>

                </div>
            </div>
        </>

    );
}

export default MainPage;