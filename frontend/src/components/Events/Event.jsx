import { useState, useEffect } from "react";
import EventForList from "./EventForList";
import { Link } from "react-router-dom";
import ListAllEventsDetail from "./ListAllEventsDetail";

const Event = () => {
    const [selectMode, setSelectMode] = useState(0);

    return (
        <div className="flex flex-col h-full min-h-fit md:rounded-2xl shadow-2xl bg-primary">
            <div className="flex max-h-full overflow-y-auto">
                <ListAllEventsDetail selectMode={selectMode}/>
            </div>
            
            <div className="flex flex-col md:flex-row h-fit min-h-fit gap-3 bg-black/5 p-4 md:rounded-b-2xl mt-auto">
                <Link className={selectMode === 0 ? "btn-nav-active" : "btn-nav"} onClick={() => setSelectMode(0)}>
                    <div className="text-2xl text-center md:p-1">
                        Vsi dogodki
                    </div>
                </Link>
                <Link className={selectMode === 1 ? "btn-nav-active" : "btn-nav"} onClick={() => setSelectMode(1)}>
                    <div className="text-2xl text-center md:p-1">
                        Trenutni dogodki
                    </div>
                </Link>
                <Link className={selectMode === 2 ? "btn-nav-active" : "btn-nav"} onClick={() => setSelectMode(2)}>
                    <div className="text-2xl text-center md:p-1">
                        Prihajajoči dogodki
                    </div>
                </Link>
                <Link className={selectMode === 3 ? "btn-nav-active" : "btn-nav"} onClick={() => setSelectMode(3)}>
                    <div className="text-2xl text-center md:p-1">
                        Pretekli dogodki
                    </div>
                </Link>
                <Link className={selectMode === 4 ? "btn-nav-active" : "btn-nav"} onClick={() => setSelectMode(4)}>
                    <div className="text-2xl text-center md:p-1">
                        Moji dogodki
                    </div>
                </Link>
                <Link to="/events/add"className="btn-nav">
                    <img
                        src="icons/plus.svg"
                        className="w-6 h-6 xl:w-8 xl:h-8 invert"
                    />
                </Link>
            </div>
        </div>
    );
};

export default Event;