import { useEffect, useState, useCallback } from "react";
import {
    MapContainer,
    TileLayer,
    GeoJSON,
    useMapEvents,
    Marker,
    Popup,
    Polygon,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import LocationNameModal from "../../components/Map/LocationNameModal";
import { fetchApi } from "../../utils/apiService";
import CreateLocationControls from "../../components/Map/CreateLocationControls";
import Notification from "../../components/Notification";
import { useLocationCreation } from "../../hooks/useLocationCreation";

const customMarkerIcon = L.icon({
    iconUrl: "/assets/custom-marker.svg",
    iconSize: [37, 50],
    iconAnchor: [19, 45],
    popupAnchor: [0, -45],
});

function onEachFeature(feature, layer) {
    if (feature.properties && feature.properties.name) {
        layer.bindPopup(feature.properties.name, { className: "custom-popup" });
    }
    if (feature.geometry.type === "Point") {
        layer.setIcon(customMarkerIcon);
    }
}

const geoJSONStyleOptions = (feature) => {
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

function LocationCreator({ onMapClick, active }) {
    useMapEvents({
        click(e) {
            if (active) {
                onMapClick(e.latlng);
            }
        },
    });
    return null;
}

const Map = () => {
    const [geojson, setGeojson] = useState(null);
    const [notification, setNotification] = useState({
        message: null,
        type: "info",
    });

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
                console.log("Fetched locations:", locations);
                const features = locations.flatMap((loc) => {
                    if (loc.longitude !== null && loc.latitude !== null) {
                        const pointFeature = {
                            type: "Feature",
                            geometry: {
                                type: "Point",
                                coordinates: [loc.longitude, loc.latitude],
                            },
                            properties: {
                                name:
                                    loc.name || loc.info || "Unnamed Location",
                                id: loc.id,
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
                                    name:
                                        loc.name || loc.info || "Unnamed Area",
                                    id: loc.id,
                                    outline_id: loc.location_outline.id,
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
                                    name:
                                        loc.name || loc.info || "Unnamed Area",
                                    id: loc.location_outline.id,
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

    const {
        mode,
        setMode,
        currentPoint,
        areaPoints,
        showNameModal,
        handleMapClick,
        handleInitiateSave,
        handleModalSave,
        handleCancel,
        handleUndoLastAreaPoint,
    } = useLocationCreation(setNotification, fetchLocations);

    return (
        <div className="h-full w-full relative">
            <Notification
                message={notification.message}
                type={notification.type}
                onClose={() => setNotification({ message: null, type: "info" })}
            />
            <MapContainer
                center={[46.554736193959975, 15.645613823633967]}
                zoom={13}
                className={`h-full md:rounded-2xl ${
                    mode !== "view" ? "cursor-cell" : ""
                }`}>
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution="&copy; OpenStreetMap contributors"
                />
                {geojson && (
                    <GeoJSON
                        key={JSON.stringify(geojson)}
                        data={geojson}
                        onEachFeature={onEachFeature}
                        style={geoJSONStyleOptions}
                    />
                )}
                <LocationCreator
                    onMapClick={handleMapClick}
                    active={mode === "creatingPoint" || mode === "creatingArea"}
                />
                {mode === "creatingPoint" && currentPoint && (
                    <Marker
                        position={[currentPoint.lat, currentPoint.lng]}
                        icon={customMarkerIcon}>
                        <Popup className="custom-popup">
                            Predogled nove lokacije
                        </Popup>
                    </Marker>
                )}
                {mode === "creatingArea" && areaPoints.length > 0 && (
                    <>
                        {areaPoints.map((point, index) => (
                            <Marker
                                key={index}
                                position={[point.lat, point.lng]}
                                icon={customMarkerIcon}>
                                <Popup>Točka {index + 1}</Popup>
                            </Marker>
                        ))}
                        {areaPoints.length > 1 && (
                            <Polygon
                                positions={areaPoints.map((p) => [
                                    p.lat,
                                    p.lng,
                                ])}
                                pathOptions={{
                                    color: "#A94A4A",
                                    fillColor: "#A94A4A",
                                    fillOpacity: 0.5,
                                    weight: 2,
                                }}
                            />
                        )}
                    </>
                )}
            </MapContainer>

            <LocationNameModal
                isOpen={showNameModal}
                onSave={handleModalSave}
                onClose={handleCancel}
            />

            <div className="absolute top-2 right-2 z-[1000] flex flex-col space-y-2">
                <CreateLocationControls
                    mode={mode}
                    onSetMode={setMode}
                    onSaveLocation={handleInitiateSave}
                    onCancelCreate={handleCancel}
                    onUndoLastPoint={handleUndoLastAreaPoint}
                    currentPointCoords={currentPoint}
                    areaPointsCount={areaPoints.length}
                />
            </div>
        </div>
    );
};

export default Map;
