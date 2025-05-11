import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Cookies from "js-cookie";

function Logout() {
    const navigate = useNavigate();

    useEffect(() => {
        // Delete the token cookie
        Cookies.remove("token");
        // Redirect to the login page
        navigate("/");
    }, [navigate]);

    return (
        <div className="flex items-center justify-center h-screen">
            <h1 className="text-2xl font-bold">Odjavljanje...</h1>
        </div>
    );
}

export default Logout;