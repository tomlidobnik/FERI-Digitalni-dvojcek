import {useState} from "react";
import { Link } from "react-router-dom";

// Match the App page button style: bg-quaternary text-white font-semibold rounded-lg
const navButton =
  "flex items-center justify-center w-full py-3 mb-2 text-md lg:text-lg font-semibold rounded-lg bg-quaternary text-white transition-colors duration-200 shadow-md hover:bg-quaternary/80 focus:bg-quaternary/90";

const LeftSidebar = () => {
    const [navShow, setNavShow] = useState(false);

    return (
        <aside className="w-full md:w-fit lg:w-64 h-fit min-h-full flex flex-col p-4 md:rounded-3xl  bg-[var(--color-primary)]">
            <div className="flex items-center justify-between md:mb-8 md:mt-2">
                <h2 className="text-2xl lg:text-3xl font-bold text-quaternary">
                    Copycats
                </h2>
                <h2 className="text-2xl lg:text-3xl font-bold text-quaternary md:hidden" onClick={() => setNavShow(!navShow)}>
                    Hide
                </h2>
            </div>
            {navShow ? (
            <>
                <nav className="flex flex-col gap-0 md:gap-2 pt-3 md:pt-0">
                <Link to="/" className={navButton}>
                    Domov
                </Link>
                <Link to="/profile" className={navButton}>
                    Profil
                </Link>
                </nav>
                <nav className="md:pt-2 flex flex-col pt-0 md:mt-auto">
                <Link to="/logout" className={navButton}>
                    Odjava
                </Link>
                </nav>
            </>
            ) : 
            <>
                <nav className="flex-col gap-0 md:gap-2 hidden md:flex">
                <Link to="/" className={navButton}>
                    Domov
                </Link>
                <Link to="/profile" className={navButton}>
                    Profil
                </Link>
                </nav>
                <nav className="md:pt-2 flex-col mt-auto pt-0 hidden md:flex">
                <Link to="/logout" className={navButton}>
                    Odjava
                </Link>
                </nav>
            </>
            }
            <div className="mt-4 text-xs text-quaternary text-center tracking-wider hidden md:block">
                &copy; {new Date().getFullYear()} Copycats
            </div>
        </aside>
    );
};

export default LeftSidebar;