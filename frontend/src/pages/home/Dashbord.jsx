import { use, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import Cookies from 'js-cookie';
import LeftSidebar from "../../components/Sidebar/LeftSidebar.jsx";
import Map from "../map/Map.jsx";

function Dashbord() {
    const navigate = useNavigate();
    useEffect(() => { // preusmeritev uporabnika če je že prijavljen
        const token = Cookies.get("token");
        if (!token) {
            navigate ("/login");
        }
    });

    return (
            <div className="flex flex-col md:flex-row h-screen">
                {/* Left Navigation Bar */}
                <div className="md:pr-0 md:p-6 md:h-full w-fit md:w-auto">
                    <LeftSidebar />
                </div>
                {/* Main Content Grid */}
                <div className="flex-1 md:p-6 ml-0 h-full">
                    <div className="grid grid-rows-3 grid-cols-1 md:grid-rows-2 md:grid-cols-2 gap-4 md:gap-6 h-full">
                        {/* Top: One big wide window (spans both columns) */}
                        <div className="row-span-1 md:row-span-1 col-span-1 md:col-span-2 rounded-2xl shadow-xl min-h-[180px] h-full w-full">
                            <Map />
                        </div>
                        {/* Bottom Left Window */}
                        <div className="row-span-1 col-span-1 rounded-2xl shadow-xl flex items-center justify-center min-h-[120px]">
                            <span className="text-xl text-gray-600">Dogodki</span>
                        </div>
                        {/* Bottom Right Window */}
                        <div className="row-span-1 col-span-1 rounded-2xl shadow-xl flex items-center justify-center min-h-[120px]">
                            <span className="text-xl text-gray-600">Prijatelji</span>
                        </div>
                    </div>
                </div>
            </div>
    );
}

export default Dashbord;