import {useState, useEffect} from "react";
import { Link, useLocation } from "react-router-dom";

// Match the App page button style: bg-quaternary text-white font-semibold rounded-lg
const navButton =
  "flex items-center justify-center w-full py-3 mb-2 text-md lg:text-lg font-semibold rounded-lg bg-quaternary text-white transition-colors duration-200 shadow-md hover:bg-quaternary/80 focus:bg-quaternary/90";

const navButtonCurrent =
  "flex items-center justify-center w-full py-3 mb-2 text-md lg:text-lg font-semibold rounded-lg bg-quaternary/70 text-white shadow-md";

const LeftSidebar = () => {
    const [navShow, setNavShow] = useState(false);
    const location = useLocation().pathname;

    useEffect(() => {
        const handleResize = () => {
            if (window.innerWidth > 1280) { // Tailwind's md breakpoint
                setNavShow(false);
            }
        };
        window.addEventListener("resize", handleResize);
        // Optionally, check on mount:
        handleResize();
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    return (
        <aside className="w-full lg:w-64 h-fit min-h-full flex flex-col p-4 lg:rounded-3xl  bg-primary">
      <div className="flex items-center justify-between lg:mb-8 lg:mt-2">
        <Link to="/">
            <h2 className="text-2xl lg:text-3xl font-bold text-text">
            Copycats
            </h2>
        </Link>
        <button
        className="lg:hidden"
        onClick={() => setNavShow((prev) => !prev)}
        aria-label="Toggle navigation"
        >
        <span className="relative inline-block w-8 h-8">
            <span
            className={`absolute inset-0 transition-opacity duration-300 ease-in-out ${
                navShow ? "opacity-100 scale-100" : "opacity-0 scale-90"
            }`}
            >
            <svg
                xmlns="http://www.w3.org/2000/svg"
                className="rounded-xl w-8 h-8 object-cover text-quaternary mr-2"
                fill="currentColor"
                viewBox="0 0 24 24"
            >
                <path d="M1.51,6.079a1.492,1.492,0,0,1,1.06.44l7.673,7.672a2.5,2.5,0,0,0,3.536,0L21.44,6.529A1.5,1.5,0,1,1,23.561,8.65L15.9,16.312a5.505,5.505,0,0,1-7.778,0L.449,8.64A1.5,1.5,0,0,1,1.51,6.079Z"/>
            </svg>
            </span>
            <span
            className={`absolute inset-0 transition-opacity duration-300 ease-in-out ${
                navShow ? "opacity-0 scale-90" : "opacity-100 scale-100"
            }`}
            >
            <svg
                xmlns="http://www.w3.org/2000/svg"
                className="rounded-xl w-8 h-8 object-cover text-quaternary mr-2"
                fill="currentColor"
                viewBox="0 0 512 512"
            >
                <path d="M480,224H32c-17.673,0-32,14.327-32,32s14.327,32,32,32h448c17.673,0,32-14.327,32-32S497.673,224,480,224z"/>
                <path d="M32,138.667h448c17.673,0,32-14.327,32-32s-14.327-32-32-32H32c-17.673,0-32,14.327-32,32S14.327,138.667,32,138.667z"/>
                <path d="M480,373.333H32c-17.673,0-32,14.327-32,32s14.327,32,32,32h448c17.673,0,32-14.327,32-32S497.673,373.333,480,373.333z"/>
            </svg>
            </span>
        </span>
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
                <Link to="/home" className={location === "/home" ? "btn-nav-active" : "btn-nav"}>
                    Domov
                </Link>
                <Link to="/profile" className={location === "/profile" ? "btn-nav-active" : "btn-nav"}>
                    Profil
                </Link>
                <Link to="/events" className={location === "/events" ? "btn-nav-active" : "btn-nav"}>
                    Dogodki
                </Link>
                </nav>
                <nav className="lg:pt-2 flex flex-col pt-0 lg:mt-auto">
                <Link to="/logout" className={navButton}>
                    Odjava
                </Link>
                </nav>
            </div>
            ) : (
            <>
                <nav className="flex-col gap-0 md:gap-2 hidden lg:flex">
                
                <Link to="/home" className={location === "/home" ? "btn-nav-active" : "btn-nav"}>
                    Domov
                </Link>
                <Link to="/profile" className={location === "/profile" ? "btn-nav-active" : "btn-nav"}>
                    Profil
                </Link>
                <Link to="/events" className={location === "/events" ? "btn-nav-active" : "btn-nav"}>
                    Dogodki
                </Link>
                </nav>
                <nav className="lg:pt-2 flex flex-col pt-0 lg:mt-auto hidden lg:block">
                <Link to="/logout" className={navButton}>
                    Odjava
                </Link>
                </nav>
            </>
            )}
            <style>
            {`
            @keyframes slideDown {
            0% {
                opacity: 0;
                transform: translateY(-20px) scaleY(0.95);
            }
            100% {
                opacity: 1;
                transform: translateY(0) scaleY(1);
            }
            }
            `}
            </style>
        </aside>
    );
};

export default LeftSidebar;