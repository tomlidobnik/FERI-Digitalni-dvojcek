import { useEffect, useState } from "react";
import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import "leaflet/dist/leaflet.css";

function onEachFeature(feature, layer) {
    if (feature.properties && feature.properties.name) {
        layer.bindPopup(feature.properties.name);
    }
}

const Map = () => {
    const [geojson, setGeojson] = useState(null);

    useEffect(() => {
        fetch("/example.geojson")
            .then((res) => res.json())
            .then((data) => setGeojson(data));
    }, []);

    return (
        <MapContainer
            center={[46.554736193959975, 15.645613823633967]}
            zoom={13}
            className="h-full md:rounded-2xl">
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
            />
            {geojson && (
                <GeoJSON data={geojson} onEachFeature={onEachFeature} />
            )}
        </MapContainer>
    );
};

export default Map;
