import { useState } from "react";

const CreateLocationControls = ({
    mode,
    onSetMode,
    onSaveLocation,
    onCancelCreate,
    onUndoLastPoint,
    currentPointCoords,
    areaPointsCount,
    onSearch,
}) => {
    const baseButtonClass =
        "text-text-custom font-bold py-2 px-4 rounded-xl shadow transition-all duration-300 ease-in-out bg-tertiary hover:brightness-90";
    const warningButtonClass = `${baseButtonClass} disabled:opacity-80 disabled:hover:brightness-100`;

    if (mode === "view") {
        return (
            <div className="space-x-2">
                <button
                    onClick={() => onSetMode("creatingPoint")}
                    className={baseButtonClass}>
                    Ustvari novo točko
                </button>
                <button
                    onClick={() => onSetMode("creatingArea")}
                    className={baseButtonClass}>
                    Ustvari novo območje
                </button>
            </div>
        );
    }

    if (mode === "creatingPoint") {
        const [searchValue, setSearchValue] = useState("");
        return (
            <div className="space-x-2 flex items-center">
                <form
                    onSubmit={e => {
                        e.preventDefault();
                        if (onSearch && searchValue.trim()) {
                            onSearch(searchValue);
                            setSearchValue("");
                        }
                    }}
                    className="flex flex-row items-center gap-2"
                    style={{ minWidth: 220, maxWidth: 350 }}
                >
                    <input
                        type="text"
                        className="flex-1 bg-white outline-none text-black text-base px-2 py-1 rounded"
                        placeholder="Poišči naslov ali lokacijo..."
                        value={searchValue}
                        onChange={e => setSearchValue(e.target.value)}
                    />
                    <button
                        type="submit"
                        className="bg-primary text-black font-bold px-3 py-1 rounded-lg hover:bg-[#e5dcc5] transition"
                    >
                        Išči
                    </button>
                </form>
                <button onClick={onSaveLocation} className={baseButtonClass}>
                    Shrani točko
                </button>
                <button onClick={onCancelCreate} className={baseButtonClass}>
                    Prekliči
                </button>
                {currentPointCoords && (
                    <div className="bg-primary p-2 rounded shadow text-sm text-text-custom">
                        Lat: {currentPointCoords.lat.toFixed(6)}, Lng:{" "}
                        {currentPointCoords.lng.toFixed(6)}
                    </div>
                )}
            </div>
        );
    }

    if (mode === "creatingArea") {
        return (
            <div className="space-x-2 flex items-center">
                <button onClick={onSaveLocation} className={baseButtonClass}>
                    Shrani območje
                </button>
                <button
                    onClick={onUndoLastPoint}
                    disabled={areaPointsCount === 0}
                    className={warningButtonClass}>
                    Razveljavi zadnjo točko
                </button>
                <button onClick={onCancelCreate} className={baseButtonClass}>
                    Prekliči
                </button>
                <div className="bg-primary p-2 rounded shadow text-sm text-text-custom">
                    Points: {areaPointsCount}
                </div>
            </div>
        );
    }

    return null;
};

export default CreateLocationControls;
