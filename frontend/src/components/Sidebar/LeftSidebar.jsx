import {useState, useEffect} from "react";
import { Link, useLocation } from "react-router-dom";
import Cookies from "js-cookie";

const LeftSidebar = () => {
    const [navShow, setNavShow] = useState(false);
    const location = useLocation().pathname;
    const userCookie = Cookies.get("user");
    const user = userCookie ? JSON.parse(userCookie) : null;

    useEffect(() => {
        const handleResize = () => {
            if (window.innerWidth > 1024) {
                setNavShow(false);
            }
        };
        window.addEventListener("resize", handleResize);
        handleResize();
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    return (
    <aside className="w-full lg:w-64 h-fit min-h-full flex flex-col p-4 lg:rounded-3xl bg-primary border-b-2 border-black/30">
      <div className="flex items-center justify-between lg:mb-8 lg:mt-2 relative">
        <Link to="/home">
            <h2 className="text-2xl lg:text-3xl font-bold text-text">
            Copycats
            </h2>
        </Link>
        {/* Hamburger menu button */}
        <button
            className="lg:hidden absolute right-0 top-0 flex flex-col justify-center items-center w-10 h-8 z-50"
            onClick={() => setNavShow((prev) => !prev)}
            aria-label="gumb"
        >
            <span
              className={`block h-0.5 w-6 bg-black transition-transform duration-300 ease-in-out transform origin-center ${
                navShow ? "rotate-45 translate-y-1.5" : ""
              }`}
            />
            <span
              className={`block h-0.5 w-6 bg-black my-1 transition-opacity duration-300 ease-in-out ${
                navShow ? "opacity-0" : "opacity-100"
              }`}
            />
            <span
              className={`block h-0.5 w-6 bg-black transition-transform duration-300 ease-in-out transform origin-center ${
                navShow ? "-rotate-45 -translate-y-1.5" : ""
              }`}
            />
        </button>
      </div>
            {navShow ? (
            <div
                className="animate-slide-down origin-top"
                style={{
                animation: "slideDown 0.3s cubic-bezier(0.4,0,0.2,1)"
                }}
            >
                <nav className="flex flex-col gap-0 lg:gap-2 pt-3 lg:pt-0">

                <Link to="/home" className={location.startsWith("/home") ? "btn-nav-active" : "btn-nav"}>
                    Domov
                </Link>
                <Link to="/profile" className={location.startsWith("/profile") ? "btn-nav-active" : "btn-nav"}>
                    Profil
                </Link>
                <Link to="/events" className={location.startsWith("/events") || location.startsWith("/event") ? "btn-nav-active" : "btn-nav"}>
                    Dogodki
                </Link>
                <Link to="/map" className={location === "/map" ? "btn-nav-active" : "btn-nav"}>
                    Mapa
                </Link>
                <Link to="/friends" className={location.startsWith("/friends") ? "btn-nav-active" : "btn-nav"}>
                    Prijatelji
                </Link>
                </nav>
                <nav className="lg:pt-2 flex flex-col pt-0 lg:mt-auto">
                <Link to="/logout" className={"btn-nav"}>
                    Odjava
                </Link>
                </nav>
            </div>
            ) : (
            <>
                <nav className="flex-col gap-0 md:gap-3 hidden lg:flex">
                <Link to="/home" className={location.startsWith("/home") ? "btn-nav-active-side" : "btn-nav-side"}>
                    <img
                    src="/icons/home.svg"
                    className="w-6 h-6 lg:w-8 lg:h-8 mr-2"
                    />
                    Domov
                </Link>
                <Link to="/events" className={location.startsWith("/events") || location.startsWith("/event") ? "btn-nav-active-side" : "btn-nav-side"}>
                    <img
                    src="/icons/calendar-day.svg"
                    className="w-6 h-6 lg:w-8 lg:h-8 mr-2"
                    />
                    Dogodki
                </Link>
                <Link to="/map" className={location.startsWith("/map") ? "btn-nav-active-side" : "btn-nav-side"}>
                    <img
                    src="/icons/map.svg"
                    className="w-6 h-6 lg:w-8 lg:h-8 mr-2"
                    />
                    Mapa
                </Link>
                <Link to="/friends" className={location.startsWith("/friends") ? "btn-nav-active-side" : "btn-nav-side"}>
                                    <img
                    src="/icons/users-alt.svg"
                    className="w-6 h-6 lg:w-8 lg:h-8 mr-2"
                    />
                    Prijatelji
                </Link>
                </nav>
                <Link to="/profile" className="mt-auto pt-16 lg:flex items-center gap-3 mb-2 hidden">
                    <div className="w-10 h-10 rounded-full bg-quaternary flex items-center justify-center text-white text-xl font-bold shadow">
                        {user ? <>{user.username ? user.username.charAt(0).toUpperCase() : "U"}</>: "U"}
                    </div>
                    <div className="flex flex-col">
                        <span className="text-lg font-semibold whitespace-nowrap overflow-hidden text-ellipsis w-42">{user ? <> {user.username ? user.username : "User"} </> : "User"}</span>
                        <span className=" text-black/60 whitespace-nowrap overflow-hidden text-ellipsis w-42 text-sm">{user ? <> {user.email ? user.email:"Email"} </>  : "Email"}</span>
                    </div>
                </Link>
                <nav className="lg:pt-2 flex flex-col pt-0 hidden lg:block border-t-4 border-black/40">
                <Link to="/logout" className="btn-nav-side">
                                    <img
                    src="/icons/user-logout.svg"
                    className="w-6 h-6 lg:w-8 lg:h-8 mr-2"
                    />
                    Odjava
                </Link>
                </nav>
            </>
            )}
        </aside>
    );
};

export default LeftSidebar;