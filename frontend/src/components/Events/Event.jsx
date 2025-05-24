import { useState } from "react";
import { Link } from "react-router-dom";
import ListAllEventsDetail from "./ListAllEventsDetail";

const Event = () => {
    const [selectMode, setSelectMode] = useState(0);
    const [menuOpen, setMenuOpen] = useState(false);

    return (
        <div className="flex flex-col min-h-[700px] h-full md:rounded-2xl shadow-2xl bg-primary">
            <div className="max-h-screen overflow-y-auto">
                <ListAllEventsDetail selectMode={selectMode}/>
            </div>
            
            {!menuOpen?  (
                <div className="md:hidden flex justify-center p-4 bg-transparent fixed mb-16 bottom-0 left-0 right-0 z-50 ">
                    <button
                        onClick={() => setMenuOpen(!menuOpen)}
                        className="p-2 rounded bg-black/20 "
                        aria-label="Odpri meni"
                    >
                            <img src="/icons/angle-up.svg" alt="Meni" className="w-8 h-8" />
                    </button>
                </div>
            ): null}

            <div className={`flex-col md:flex-row h-fit gap-2 bg-black/10 p-4 md:rounded-b-2xl mt-auto fixed bottom-0 w-full md:static z-50 mb-15 md:mb-0 backdrop-blur-sm
                ${menuOpen ? "flex" : "hidden"} md:flex`}>
                <div className="w-full flex justify-center md:hidden">
                    <button
                        onClick={() => setMenuOpen(!menuOpen)}
                        className="p-2 rounded bg-black/10 w-fit"
                        aria-label="Odpri meni"
                    >
                        <img src="/icons/angle-down.svg" alt="Meni" className="w-8 h-8" />
                    </button>
                </div>
                <Link className={selectMode === 0 ? "btn-nav-active" : "btn-nav"} onClick={() => { setSelectMode(0); setMenuOpen(false); }}>
                    <div className="text-2xl text-center md:p-1">Vsi dogodki</div>
                </Link>
                <Link className={selectMode === 1 ? "btn-nav-active" : "btn-nav"} onClick={() => { setSelectMode(1); setMenuOpen(false); }}>
                    <div className="text-2xl text-center md:p-1">Trenutni dogodki</div>
                </Link>
                <Link className={selectMode === 2 ? "btn-nav-active" : "btn-nav"} onClick={() => { setSelectMode(2); setMenuOpen(false); }}>
                    <div className="text-2xl text-center md:p-1">Prihajajoči dogodki</div>
                </Link>
                <Link className={selectMode === 3 ? "btn-nav-active" : "btn-nav"} onClick={() => { setSelectMode(3); setMenuOpen(false); }}>
                    <div className="text-2xl text-center md:p-1">Pretekli dogodki</div>
                </Link>
                <Link className={selectMode === 4 ? "btn-nav-active" : "btn-nav"} onClick={() => { setSelectMode(4); setMenuOpen(false); }}>
                    <div className="text-2xl text-center md:p-1">Moji dogodki</div>
                </Link>
                <Link to="/events/add" className="btn-nav" onClick={() => setMenuOpen(false)}>
                    <img src="icons/plus.svg" className="w-6 h-6 xl:w-8 xl:h-8 invert" />
                </Link>
            </div>
        </div>
    );
};

export default Event;