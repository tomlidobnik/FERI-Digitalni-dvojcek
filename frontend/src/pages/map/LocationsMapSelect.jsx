import { useEffect, useState, useCallback, useRef } from "react";
import {
    MapContainer,
    TileLayer,
    GeoJSON,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import { fetchApi } from "../../utils/apiService";
import Notification from "../../components/Notification";

const customMarkerIcon = L.icon({
    iconUrl: "/assets/custom-marker.svg",
    iconSize: [37, 50],
    iconAnchor: [19, 45],
    popupAnchor: [0, -45],
});

const customMarkerIconGreen = L.icon({
    iconUrl: "/assets/custom-marker-blue.svg",
    iconAnchor: [19, 45],
    popupAnchor: [0, -45],
});


const geoJSONStyleOptions = (feature, selectedLocationUp) => {
    if (
        feature.geometry.type === "Polygon" &&
        selectedLocationUp &&
        (feature.properties.id === selectedLocationUp.id ||
         feature.properties.outline_id === selectedLocationUp.id)
    ) {
        return {
            color: "#69A1DD",
            weight: 3,
            opacity: 1,
            fillColor: "#69A1DD",
            fillOpacity: 0.4,
        };
    }
    if (feature.geometry.type === "Polygon") {
        return {
            color: "#A94A4A",
            weight: 2,
            opacity: 1,
            fillColor: "#A94A4A",
            fillOpacity: 0.3,
        };
    }
    return {};
};

const LocationsMapSelect = ({ onLocationSelect, selectedLocationUp }) => {
    const [geojson, setGeojson] = useState(null);
    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });
    const [selectedLocation, setSelectedLocation] = useState(null);

    // Ref to avoid stale closure in onEachFeature
    const setSelectedLocationRef = useRef(setSelectedLocation);
    setSelectedLocationRef.current = setSelectedLocation;

    const fetchLocations = useCallback(async () => {
        try {
            const locations = await fetchApi("/location/list", {
                method: "GET",
                headers: {
                    "Cache-Control": "no-cache, no-store, must-revalidate",
                    Pragma: "no-cache",
                    Expires: "0",
                },
            });

            if (locations) {
                const features = locations.flatMap((loc) => {
                    if (loc.longitude !== null && loc.latitude !== null) {
                        const pointFeature = {
                            type: "Feature",
                            geometry: {
                                type: "Point",
                                coordinates: [loc.longitude, loc.latitude],
                            },
                            properties: {
                                name: loc.name || loc.info || "Unnamed Location",
                                id: loc.id,
                                info: loc.info,
                            },
                        };
                        if (
                            loc.location_outline &&
                            loc.location_outline.points &&
                            loc.location_outline.points.length > 0
                        ) {
                            const polygonFeature = {
                                type: "Feature",
                                geometry: {
                                    type: "Polygon",
                                    coordinates: [
                                        loc.location_outline.points.map((p) => [
                                            p.longitude,
                                            p.latitude,
                                        ]),
                                    ],
                                },
                                properties: {
                                    name: loc.name || loc.info || "Unnamed Area",
                                    id: loc.id,
                                    outline_id: loc.location_outline.id,
                                    info: loc.info,
                                },
                            };
                            return [pointFeature, polygonFeature];
                        }
                        return [pointFeature];
                    } else if (
                        loc.location_outline &&
                        loc.location_outline.points &&
                        loc.location_outline.points.length > 0
                    ) {
                        return [
                            {
                                type: "Feature",
                                geometry: {
                                    type: "Polygon",
                                    coordinates: [
                                        loc.location_outline.points.map((p) => [
                                            p.longitude,
                                            p.latitude,
                                        ]),
                                    ],
                                },
                                properties: {
                                    name: loc.name || loc.info || "Unnamed Area",
                                    id: loc.id,
                                    info: loc.info,
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
            } else {
                setGeojson(null);
            }
        } catch (error) {
            setNotification({
                message: `Napaka pri pridobivanju lokacij: ${error.message}`,
                type: "error",
            });
            setGeojson(null);
        }
    }, []);

    useEffect(() => {
        fetchLocations();
    }, [fetchLocations]);

    // Custom onEachFeature to handle click
function onEachFeature(feature, layer) {
    if (feature.properties && feature.properties.name) {
        layer.bindPopup(feature.properties.name, { className: "custom-popup" });
    }
    if (feature.geometry.type === "Point") {
        // Set icon based on whether this point is the selected location
        const isSelected =
            selectedLocationUp &&
            (
                feature.properties.id === selectedLocationUp.id
            );
        layer.setIcon(isSelected ? customMarkerIconGreen : customMarkerIcon);
    }
    layer.on("click", () => {
        setSelectedLocationRef.current(feature.properties);
        if (onLocationSelect) {
            onLocationSelect(feature.properties);
        }
        // eslint-disable-next-line no-console
        console.log("Selected location:", feature.properties);
    });
}

    // Show notification when selectedLocation changes
    useEffect(() => {
        if (!geojson) return;

        document.querySelectorAll('.leaflet-marker-icon').forEach((iconEl) => {
            if (selectedLocation) {
                setNotification({
                    message: `Izbrana lokacija: ${selectedLocation.name || "Brez imena"}${selectedLocation.info ? "" : ""}`,
                    type: "info",
                });
            }
        });
    }, [selectedLocation, geojson]);

    return (
    <div className="h-full w-full relative">
        <Notification
            message={notification.message}
            type={notification.type}
            onClose={() => setNotification({ message: null, type: "info" })}
            className="absolute top-2 right-2 z-[1000]"
        />
        <MapContainer
            center={[46.554736193959975, 15.645613823633967]}
            zoom={13}
            className="h-full md:rounded-2xl"
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
            />
            {geojson && (
                <GeoJSON
                    key={JSON.stringify(geojson) + JSON.stringify(selectedLocationUp)}
                    data={geojson}
                    onEachFeature={onEachFeature}
                    style={feature => geoJSONStyleOptions(feature, selectedLocationUp)}
                />
            )}
        </MapContainer>
    </div>
    );
};

export default LocationsMapSelect;