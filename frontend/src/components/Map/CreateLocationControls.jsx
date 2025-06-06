const CreateLocationControls = ({
    mode,
    onSetMode,
    onSaveLocation,
    onCancelCreate,
    onUndoLastPoint,
    currentPointCoords,
    areaPointsCount,
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
        return (
            <div className="space-x-2 flex items-center">
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
