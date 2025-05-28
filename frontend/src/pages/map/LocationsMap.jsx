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
        fetch(`https://${API_URL}/api/location/list`)
            .then((res) => res.json())
            .then((locations) => {
                console.log("Fetched locations:", locations);
                const features = locations.flatMap((loc) => {
                    if (loc.longitude !== null && loc.latitude !== null) {
                        return [
                            {
                                type: "Feature",
                                geometry: {
                                    type: "Point",
                                    coordinates: [loc.longitude, loc.latitude],
                                },
                                properties: {
                                    id: loc.id,
                                    name: loc.info || `Location ${loc.id}`,
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
                                    id: loc.id,
                                    name: loc.info || `Location ${loc.id}`,
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
