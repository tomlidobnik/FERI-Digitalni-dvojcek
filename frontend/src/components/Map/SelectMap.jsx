import Map from "../../pages/map/LocationsMapSelect";

export default function SelectMap({ onLocationSelect, selectedLocation }) {
    return (
        <div className="h-96 pt-4 z-0">
            <Map onLocationSelect={onLocationSelect} selectedLocationUp={selectedLocation} />
        </div>
    );
}