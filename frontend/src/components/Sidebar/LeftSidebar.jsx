import React from "react";
import { Link } from "react-router-dom";

// Match the App page button style: bg-quaternary text-white font-semibold rounded-lg
const navButton =
  "flex items-center justify-center w-full py-3 mb-2 text-md lg:text-lg font-semibold rounded-lg bg-quaternary text-white transition-colors duration-200 shadow-md hover:bg-quaternary/80 focus:bg-quaternary/90";

const LeftSidebar = () => {
  return (
    <aside className=" w-fit lg:w-64 h-fit min-h-full flex flex-col p-4 rounded-3xl  bg-[var(--color-primary)]">
      <h2 className="text-2xl lg:text-3xl font-bold mb-8 mt-2 text-quaternary">
        Copycats
      </h2>
      <nav className="flex flex-col gap-2">
        <Link to="/" className={navButton}>
          Domov
        </Link>
        <Link to="/profile" className={navButton}>
          Profil
        </Link>
      </nav>
      <nav className="pt-2 flex flex-col mt-auto">
        <Link to="/logout" className={navButton}>
          Odjava
        </Link>
      </nav>
      <div className="mt-4 text-xs text-quaternary text-center tracking-wider">
        &copy; {new Date().getFullYear()} Copycats
      </div>
    </aside>
  );
};

export default LeftSidebar;