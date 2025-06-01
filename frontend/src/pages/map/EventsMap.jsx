import { useEffect, useState, useRef } from "react";
import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import markerIconUrl from "/assets/custom-marker.svg"; // adjust path if needed

const customMarkerIcon = L.icon({
    iconUrl: markerIconUrl,
    iconSize: [37, 50],
    iconAnchor: [19, 45],
    popupAnchor: [0, -45],
});


function Map() {
    const [geojson, setGeojson] = useState(null);
    const [events, setEvents] = useState([]);
    const [selectedLocationId, setSelectedLocationId] = useState(null); // <-- Add this
    const API_URL = import.meta.env.VITE_API_URL;
    const mapRef = useRef();

    useEffect(() => {
        fetch(`https://${API_URL}/api/event/available`)
            .then((res) => res.json())
            .then((eventsData) => {
                setEvents(eventsData);
                const features = eventsData.flatMap((event) => {
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
                                    location_id: loc.location_id || loc.id,
                                    event_id: event.id,
                                    event_title: event.title || `Event ${event.id}`,
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
                                    location_id: loc.location_id || loc.id,
                                    event_id: event.id,
                                    event_title: event.title || `Event ${event.id}`,
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
    }, [API_URL]);

    // Group events by location_id for popup display
    const eventsByLocation = {};
    events.forEach(event => {
        const loc = event.location;
        const locId = loc?.location_id || loc?.id;
        if (!locId) return;
        if (!eventsByLocation[locId]) eventsByLocation[locId] = [];
        eventsByLocation[locId].push(event);
    });

    function onEachFeature(feature, layer) {
        // Set custom icon for Point features
        if (feature.geometry.type === "Point") {
            layer.setIcon(customMarkerIcon);
        }
        const locationId = feature.properties.location_id;
        if (locationId && eventsByLocation[locationId]) {
            const eventsList = eventsByLocation[locationId]
                .map(ev => `<li>${ev.title}</li>`)
                .join("");
            layer.bindPopup(
                `<b>Dogodki na tej lokaciji:</b><ul>${eventsList}</ul>`
            );
        } else if (feature.properties && feature.properties.event_title) {
            layer.bindPopup(feature.properties.event_title);
        }
    }

    function geoJSONStyleOptions(feature) {
        if (
            feature.geometry.type === "Polygon" &&
            selectedLocationId &&
            feature.properties.location_id === selectedLocationId
        ) {
            return {
                color: "#2ecc40", // green border
                weight: 3,
                opacity: 1,
                fillColor: "#2ecc40", // green fill
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
    }


    const defaultPosition = [46.5547, 15.6467];

    return (
        <MapContainer
            center={defaultPosition}
            zoom={13}
            className="h-full md:rounded-2xl"
            ref={mapRef}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            {geojson && (
                <GeoJSON
                    data={geojson}
                    onEachFeature={onEachFeature}
                    style={geoJSONStyleOptions}
                    key={JSON.stringify(geojson) + selectedLocationId}
                />
            )}
        </MapContainer>
    );
}

export default Map;