import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Cookies from 'js-cookie';
import LeftSidebar from "../../components/Sidebar/LeftSidebar.jsx";
import Map from "../map/LocationsMap.jsx";
import ListAllEvents from "../../components/Events/ListAllEvents.jsx";
import ListAllFriends from "../../components/Friends/ListAllFriends.jsx";

function Dashbord() {

    const navigate = useNavigate();

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
                <div className="flex-1  ml-0 h-full overflow-y-auto">
                    <div className="grid grid-cols-1 md:grid-rows-2 md:grid-cols-2 md:gap-6 h-fit min-h-screen md:p-6">
                        <div className="row-span-1 md:row-span-1 col-span-1 md:col-span-2 md:rounded-2xl shadow-xl min-h-[500px] h-[calc(50vh-4rem)] md:h-full w-full">
                            <Map />
                        </div>
                        <div className="md:flex min-h-[500px] h-full md:rounded-2xl md:shadow-xl max-h-[700px]">
                            <ListAllEvents />
                        </div>
                        <div className="md:flex min-h-[500px] h-full row-span-1 col-span-1 md:rounded-2xl md:shadow-xl">
                            <ListAllFriends />
                        </div>
                    </div>
                    <div className="text-lg bg-quaternary text-center py-4 lg:hidden block relative bottom-0 left-0 right-0">
                        {new Date().getFullYear()} Copycats
                    </div>
                </div>
            </div>
        </>

    );
}

export default Dashbord;