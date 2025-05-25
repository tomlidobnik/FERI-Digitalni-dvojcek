import { useState, useEffect } from "react";
import EventForListDetail from "./EventForListDetail";
import { Link } from "react-router-dom";
import Cookies from 'js-cookie';
import { useSelector } from "react-redux";

const ListAllEventsDetail = ({selectMode}) => {
    //const[selectMode, setSelectMode] = useState(useSelector((state) => state.eventFilter.value));
    const API_URL = import.meta.env.VITE_API_URL;

    const [response, setResponse] = useState([]);
    const [filteredResponse, setFilteredResponse] = useState([]);
    const [dogodkiTitle, setDogodkiTitle] = useState("Dogodki");

    useEffect(() => {
        console.log(selectMode);
        fetch(`https://${API_URL}/api/event/all`)
            .then((res) => res.json())
            .then((data) => {
                setResponse(data.reverse());
            })
            .catch((err) => {
                console.error(err);
            });
    }, [API_URL]);

    useEffect(() => {
        switch (selectMode) {
            case 0:
                setDogodkiTitle("Vsi dogodki");
                break;
            case 1:
                setDogodkiTitle("Trenutni dogodki");
                break;
            case 2:
                setDogodkiTitle("Prihajajoči dogodki");
                break;
            case 3:
                setDogodkiTitle("Pretekli dogodki");
                break;
            case 4:
                setDogodkiTitle("Moji dogodki");
                break;
            default:
                setDogodkiTitle("Dogodki");
                break;
        }
        const token = Cookies.get("token");
        if (selectMode === 4){
            fetch(`https://${API_URL}/api/event/my`,{
                headers: {
                    Authorization: token ? `Bearer ${token}` : "",
                },
            })
            .then((res) => res.json())
            .then((data) => {
                setResponse(data.reverse());
            })
            .catch((err) => {
                console.error(err);
            });
        }else{
            fetch(`https://${API_URL}/api/event/all`)
            .then((res) => res.json())
            .then((data) => {
                setResponse(data.reverse());
            })
            .catch((err) => {
                console.error(err);
            });
        }
        const now = new Date();
        const filtered = response.filter(event => {
            const start = new Date(event.start_date);
            const end = new Date(event.end_date);

            if (selectMode === 0) return true;
            if (selectMode === 1) return start <= now && end >= now;
            if (selectMode === 2) return start > now;
            if (selectMode === 3) return end < now;
            if (selectMode === 4) return true;
            return true;
        });
        setFilteredResponse(filtered);
    }, [selectMode]);

    useEffect(() => {
        const now = new Date();
        const filtered = response.filter(event => {
            const start = new Date(event.start_date);
            const end = new Date(event.end_date);

            if (selectMode === 0) return true;
            if (selectMode === 1) return start <= now && end >= now;
            if (selectMode === 2) return start > now;
            if (selectMode === 3) return end < now;
            if (selectMode === 4) return true;
            return true;
        });
        setFilteredResponse(filtered);
    }, [response]);

    //console.log("Filtered Events:", filteredEvents);

    return (
    <div className="w-full flex flex-col h-full">
        <h1 className="text-2xl p-4 xl:p-6 xl:text-5xl font-bold text-text bg-black/10 md:rounded-t-2xl flex-shrink-0">
            <span className="hidden md:inline">Dogodki</span>
            <span className="md:hidden">{dogodkiTitle}</span>
        </h1>
        <div className="flex-1 overflow-y-auto p-2 sm:p-4">
            {filteredResponse.length === 0 ? (
                <div className="text-center text-text/70 text-lg py-8">Ni dogodkov za prikaz.</div>
            ) : (
                filteredResponse.map((event) => (
                    <EventForListDetail
                        key={event.id}
                        event={event}
                        selectMode={selectMode}
                    />
                ))
            )}
        </div>
    </div>
    );
};

export default ListAllEventsDetail;