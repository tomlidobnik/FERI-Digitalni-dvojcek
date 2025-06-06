import { useState, useEffect, useCallback } from "react";
import { fetchApi } from "../utils/apiService";

export const useLocationCreation = (setNotification, fetchLocations) => {
    const [mode, setMode] = useState("view");
    const [currentPoint, setCurrentPoint] = useState(null);
    const [areaPoints, setAreaPoints] = useState([]);
    const [showNameModal, setShowNameModal] = useState(false);

    useEffect(() => {
        if (mode === "creatingPoint" || mode === "creatingArea") {
            setCurrentPoint(null);
            setAreaPoints([]);
        }
    }, [mode]);

    const handleMapClick = useCallback(
        (latlng) => {
            if (mode === "creatingPoint") {
                setCurrentPoint(latlng);
            } else if (mode === "creatingArea") {
                setAreaPoints((prevPoints) => [...prevPoints, latlng]);
            }
        },
        [mode]
    );

    const handleCancel = useCallback(() => {
        setMode("view");
        setCurrentPoint(null);
        setAreaPoints([]);
        setShowNameModal(false);
    }, []);

    const confirmSavePointLocation = useCallback(
        async (locationName) => {
            if (!currentPoint) {
                setNotification({
                    message:
                        "Prosimo, najprej izberite lokacijo na zemljevidu.",
                    type: "warning",
                });
                return;
            }
            if (!locationName) {
                setNotification({
                    message: "Prosimo, vnesite ime za lokacijo.",
                    type: "warning",
                });
                return;
            }

            try {
                await fetchApi("/location/create", {
                    method: "POST",
                    body: {
                        latitude: currentPoint.lat,
                        longitude: currentPoint.lng,
                        info: locationName,
                        name: locationName,
                        location_outline_fk: null,
                    },
                });
                setNotification({
                    message: "Točkovna lokacija uspešno shranjena!",
                    type: "success",
                });
                fetchLocations();
                handleCancel();
            } catch (error) {
                setNotification({
                    message: `Napaka pri shranjevanju točkovne lokacije: ${error.message}`,
                    type: "error",
                });
            }
        },
        [currentPoint, fetchLocations, handleCancel, setNotification]
    );

    const confirmSaveAreaLocation = useCallback(
        async (areaName) => {
            if (areaPoints.length < 3) {
                setNotification({
                    message: "Območje mora imeti vsaj 3 točke.",
                    type: "warning",
                });
                return;
            }
            if (!areaName) {
                setNotification({
                    message: "Prosimo, vnesite ime za območje.",
                    type: "warning",
                });
                return;
            }

            try {
                await fetchApi("/location_outline/create", {
                    method: "POST",
                    body: {
                        info: areaName,
                        points: areaPoints.map((p) => ({
                            latitude: p.lat,
                            longitude: p.lng,
                        })),
                    },
                });
                setNotification({
                    message: "Lokacija območja uspešno shranjena!",
                    type: "success",
                });
                fetchLocations();
                handleCancel();
            } catch (error) {
                setNotification({
                    message: `Napaka pri shranjevanju lokacije območja: ${error.message}`,
                    type: "error",
                });
            }
        },
        [areaPoints, fetchLocations, handleCancel, setNotification]
    );

    const handleInitiateSave = useCallback(() => {
        if (mode === "creatingPoint" && currentPoint) {
            setShowNameModal(true);
        } else if (mode === "creatingArea" && areaPoints.length >= 3) {
            setShowNameModal(true);
        } else {
            const message =
                mode === "creatingPoint"
                    ? "Prosimo, najprej kliknite na zemljevid, da izberete točko."
                    : "Prosimo, narišite vsaj 3 točke na zemljevidu za območje.";
            setNotification({ message, type: "warning" });
        }
    }, [mode, currentPoint, areaPoints, setNotification]);

    const handleModalSave = useCallback(
        (name) => {
            if (mode === "creatingPoint") {
                confirmSavePointLocation(name);
            } else if (mode === "creatingArea") {
                confirmSaveAreaLocation(name);
            }
            setShowNameModal(false);
        },
        [mode, confirmSavePointLocation, confirmSaveAreaLocation]
    );

    const handleUndoLastAreaPoint = useCallback(() => {
        if (mode === "creatingArea") {
            setAreaPoints((prevPoints) => prevPoints.slice(0, -1));
        }
    }, [mode]);

    return {
        mode,
        setMode,
        currentPoint,
        areaPoints,
        showNameModal,
        setShowNameModal,
        handleMapClick,
        handleInitiateSave,
        handleModalSave,
        handleCancel,
        handleUndoLastAreaPoint,
    };
};
