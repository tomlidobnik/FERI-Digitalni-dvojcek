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
    const API_URL = import.meta.env.VITE_API_URL;
    useEffect(() => {
        fetch(`https://${API_URL}/api/event/available`)
            .then((res) => res.json())
            .then((events) => {
                console.log("Fetched events:", events);
                const features = events.flatMap((event) => {
                    const loc = event.location;
                    if (!loc) return [];
                    if (loc.longitude !== null && loc.latitude !== null) {
                        return [
                            {
                                type: "Feature",
                                geometry: {
                                    type: "Point",
                                    coordinates: [loc.longitude, loc.latitude],
                                },
                                properties: {
                                    id: event.id,
                                    name: event.title || `Event ${event.id}`,
                                },
                            },
                        ];
                    } else if (
                        loc.location_outline &&
                        Array.isArray(loc.location_outline.points)
                    ) {
                        const coords = loc.location_outline.points.map((pt) => [
                            pt.longitude,
                            pt.latitude,
                        ]);
                        if (
                            coords.length > 0 &&
                            (coords[0][0] !== coords[coords.length - 1][0] ||
                                coords[0][1] !== coords[coords.length - 1][1])
                        ) {
                            coords.push(coords[0]);
                        }
                        return [
                            {
                                type: "Feature",
                                geometry: {
                                    type: "Polygon",
                                    coordinates: [coords],
                                },
                                properties: {
                                    id: event.id,
                                    name: event.title || `Event ${event.id}`,
                                },
                            },
                        ];
                    }
                    return [];
                });

                setGeojson({
                    type: "FeatureCollection",
                    features,
                });
            });
    }, []);

    return (
        <MapContainer
            center={[46.554736193959975, 15.645613823633967]}
            zoom={13}
            style={{ height: "100vh", width: "100%" }}>
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
