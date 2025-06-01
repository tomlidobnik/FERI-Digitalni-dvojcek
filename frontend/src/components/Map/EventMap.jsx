import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polygon } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import markerIconUrl from "/assets/custom-marker.svg";

const customMarkerIcon = L.icon({
    iconUrl: markerIconUrl,
    iconSize: [37, 50],
    iconAnchor: [19, 45],
    popupAnchor: [0, -45],
});

const EventMap = ({ location , outline}) => {
    const API_URL = import.meta.env.VITE_API_URL;
    const [error, setError] = useState(null);

    if (error) {
        return <div className="text-red-500">{error}</div>;
    }

    if (!location) {
        return      
            <div className="flex items-center justify-center h-full pt-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-text"></div>
            </div>;
    }

    const hasCoords = location.longitude !== null && location.latitude !== null;
    const hasPolygon =
        outline &&
        outline.points &&
        outline.points.length > 0;

    // Polygon coordinates in [lat, lng] format for Leaflet
    const polygonCoords = hasPolygon
        ? outline.points.map(p => [p.latitude, p.longitude])
        : [];

    // Center map on marker or polygon
    const center = hasCoords
        ? [location.latitude, location.longitude]
        : (polygonCoords[0] || [46.5460, 15.6467]);

    return (
        <MapContainer
            center={center}
            zoom={14}
            className="h-full w-full rounded-2xl"
            style={{ minHeight: 300 }}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
            />
            {hasCoords && (
                <Marker position={[location.latitude, location.longitude]} icon={customMarkerIcon}>
                    <Popup>
                        {location.name || location.info || "Lokacija"}
                    </Popup>
                </Marker>
            )}
            {hasPolygon && (
                <Polygon positions={polygonCoords} pathOptions={{ color: "#69A1DD", fillOpacity: 0.4 }}>
                    <Popup>
                        {location.name || location.info || "Lokacija"}
                    </Popup>
                </Polygon>
            )}
        </MapContainer>
    );
};

export default EventMap;