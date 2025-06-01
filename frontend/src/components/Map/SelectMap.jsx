import Map from "../../pages/map/LocationsMapSelect";

export default function SelectMap({ onLocationSelect }) {
    return (
        <div className="h-96 pt-4">
            <Map onLocationSelect={onLocationSelect} />
        </div>
    );
}