import { useState, useEffect } from "react";
import EventForListDetail from "./EventForListDetail";
import { Link } from "react-router-dom";
import Cookies from "js-cookie";

const ListAllEventsDetail = ({ selectMode }) => {
    const API_URL = import.meta.env.VITE_API_URL;
    const [isLoading, setIsLoading] = useState(true);
    const [response, setResponse] = useState([]);
    const [filteredResponse, setFilteredResponse] = useState([]);
    const [dogodkiTitle, setDogodkiTitle] = useState("Dogodki");
    const [allLocations, setAllLocations] = useState([]);
    const [searchValue, setSearchValue] = useState("");
    const [selectedTag, setSelectedTag] = useState("");
    const [allTags, setAllTags] = useState([]);

    useEffect(() => {
        setIsLoading(true);
        setDogodkiTitle(
            [
                "Vsi dogodki",
                "Trenutni dogodki",
                "Prihajajoči dogodki",
                "Pretekli dogodki",
                "Moji dogodki",
            ][selectMode] || "Dogodki"
        );
        const token = Cookies.get("token");
        const url =
            selectMode === 4
                ? `https://${API_URL}/api/event/my`
                : `https://${API_URL}/api/event/all`;
        const headers =
            selectMode === 4 && token
                ? { Authorization: `Bearer ${token}` }
                : {};
        fetch(`https://${API_URL}/api/location/all`)
            .then((res) => res.json())
            .then((data) => setAllLocations(data))
            .catch(() => setAllLocations([]));
        fetch(url, { headers })
            .then((res) => res.json())
            .then((data) => {
                setResponse(data.reverse());
                const tags = Array.from(
                    new Set(data.map((ev) => ev.tag).filter(Boolean))
                );
                setAllTags(tags);
            })
            .catch(() => {
                setResponse([]);
                setAllTags([]);
            });
    }, [API_URL, selectMode]);

    // Filter events and handle loading state
    useEffect(() => {
        setIsLoading(true);
        const now = new Date();
        let filtered = response.filter((event) => {
            const start = new Date(event.start_date);
            const end = new Date(event.end_date);
            if (selectMode === 0) return true;
            if (selectMode === 1) return start <= now && end >= now;
            if (selectMode === 2) return start > now;
            if (selectMode === 3) return end < now;
            if (selectMode === 4) return true;
            return true;
        });
        if (selectedTag) {
            filtered = filtered.filter((event) => event.tag === selectedTag);
        }
        if (searchValue) {
            filtered = filtered.filter((event) =>
                event.title.toLowerCase().includes(searchValue.toLowerCase())
            );
        }
        setFilteredResponse(filtered);
        setTimeout(() => setIsLoading(false), 300);
    }, [response, selectMode, selectedTag, searchValue]);

    const handleTagClick = (tag) => setSelectedTag(tag);
    const handleSearchChange = (e) => setSearchValue(e.target.value);

    return (
        <div className="w-full flex flex-col h-full">
            <h1 className="text-2xl p-4 xl:p-6 xl:text-5xl font-bold text-text bg-black/10 md:rounded-t-2xl flex-shrink-0">
                <span className="hidden md:inline">Dogodki</span>
                <span className="md:hidden">{dogodkiTitle}</span>
            </h1>
            {/* Search and tag filter bar */}
            <div className="p-2 flex flex-col gap-2">
                <input
                    type="text"
                    placeholder="Išči po imenu dogodka..."
                    value={searchValue}
                    onChange={handleSearchChange}
                    className="p-2 rounded border border-gray-300 w-full text-base"
                />
                <div className="flex flex-wrap gap-2 mt-1">
                    <button
                        className={`px-3 py-1 rounded ${
                            !selectedTag ? "bg-primary/60" : "bg-primary/20"
                        } hover:bg-primary/60 text-xs font-semibold`}
                        onClick={() => handleTagClick("")}>
                        Vse
                    </button>
                    {allTags.map((tag) => (
                        <button
                            key={tag}
                            className={`px-3 py-1 rounded ${
                                selectedTag === tag
                                    ? "bg-primary/60"
                                    : "bg-primary/20"
                            } hover:bg-primary/60 text-xs font-semibold flex items-center gap-1`}
                            onClick={() => handleTagClick(tag)}>
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                                strokeWidth={1.5}
                                stroke="currentColor"
                                className="w-4 h-4">
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M2.25 12.75V6.375c0-.621.504-1.125 1.125-1.125h6.375c.298 0 .584.118.796.329l9.375 9.375a1.125 1.125 0 010 1.591l-6.375 6.375a1.125 1.125 0 01-1.591 0l-9.375-9.375a1.125 1.125 0 010-1.591z"
                                />
                                <circle
                                    cx="7.5"
                                    cy="7.5"
                                    r="1.5"
                                    fill="currentColor"
                                />
                            </svg>
                            {tag}
                        </button>
                    ))}
                </div>
            </div>
            <div className="flex-1 overflow-y-auto p-2 sm:p-4">
                {isLoading ? (
                    <div className="flex items-center justify-center h-full pt-8">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
                    </div>
                ) : filteredResponse.length < 1 ? (
                    <div className="text-center text-text/70 text-lg py-8">
                        Ni dogodkov za prikaz.
                    </div>
                ) : (
                    filteredResponse.map((event) => {
                        const locationEvent = allLocations.find(
                            (loc) => loc.id === event.location_fk
                        );
                        return (
                            <EventForListDetail
                                key={event.id}
                                event={event}
                                selectMode={selectMode}
                                location={locationEvent}
                            />
                        );
                    })
                )}
            </div>
        </div>
    );
};

export default ListAllEventsDetail;
