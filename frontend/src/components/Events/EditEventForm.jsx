import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import Cookies from "js-cookie";
import { useEffect, useState } from "react";
import DateTimePicker from "react-datetime-picker";
import "react-datetime-picker/dist/DateTimePicker.css";
import "react-calendar/dist/Calendar.css";
import "react-clock/dist/Clock.css";
import { useSelector } from "react-redux";
import SelectMap from "../Map/SelectMap";

export default function EditEventForm() {
    const navigate = useNavigate();
    const event = useSelector((state) => state.event);
    const [startTime, setStartTime] = useState(new Date());
    const [endTime, setEndTime] = useState(new Date());
    const [selectedLocation, setSelectedLocation] = useState(
        event.location || null
    );
    const [tag, setTag] = useState(event.tag || "");

    const tagOptions = [
        { value: "", label: "Brez oznake" },
        { value: "sport", label: "Šport" },
        { value: "school", label: "Šola" },
        { value: "event", label: "Dogodek" },
        { value: "other", label: "Drugo" },
    ];

    const changeStartTime = (value) => {
        setStartTime(value);
        clearErrors("starttime");
        clearErrors("endtime");
    };

    const changeEndTime = (value) => {
        setEndTime(value);
        clearErrors("endtime");
        clearErrors("starttime");
    };

    const pad = (num) => num.toString().padStart(2, "0");
    const toLocalISOString = (date) => {
        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
            date.getDate()
        )}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
    };

    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors },
    } = useForm({
        defaultValues: {
            public: event.public ? "true" : "false",
            title: event.title || "",
            description: event.description || "",
        },
    });

    useEffect(() => {
        // preusmeritev uporabnika če je že prijavljen
        const token = Cookies.get("token");
        if (!token) {
            navigate("/login");
        }
        if (event.title === "") {
            navigate("/events");
            return;
        } else {
            setStartTime(new Date(event.start_date));
            setEndTime(new Date(event.end_date));
        }
        //console.log(event);
        if (event.location_fk) {
            fetch(
                `https://${import.meta.env.VITE_API_URL}/api/location/by_id/${
                    event.location_fk
                }`
            )
                .then((res) => res.json())
                .then((data) => {
                    setSelectedLocation(data);
                    //console.log(data);
                })
                .catch(() => setSelectedLocation(null));
        }
    }, [navigate, event]);

    const onSubmit = async (data) => {
        console.log("Submitting event data:", data);
    console.log("selected location id:",selectedLocation.id)
    console.log("selected location id:",selectedLocation)
    clearErrors();
        try {
            const token = Cookies.get("token");
            const eventData = {
                id: event.id,
                title: data.title,
                description: data.description,
                start_date: toLocalISOString(startTime),
                end_date: toLocalISOString(endTime),
                location_fk: selectedLocation ? selectedLocation.id : null,
                public: data.public === "true",
                tag: tag || null,
            };
            if (startTime >= endTime) {
                setError("starttime", {
                    message: "Začetni čas mora biti pred končnim časom",
                });
                return;
            } else {
                const response = await fetch(
                    `https://${import.meta.env.VITE_API_URL}/api/event/update`,
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/json",
                            Authorization: `Bearer ${token}`,
                        },
                        body: JSON.stringify(eventData),
                    }
                );
                if (!response.ok) {
                    const errorData = await response.json();
                    if (errorData.error) {
                        setError("root", {
                            message: errorData.error,
                        });
                    } else {
                        setError("root", {
                            message: "Napaka pri ustvarjanju dogodka",
                        });
                    }
                    return;
                } else if (response.ok) {
                    navigate("/events");
                }
            }
        } catch (error) {
            setError("root", {
                message: "Napaka ustvarjanju dogodka (client)",
            });
        }
    };

    return (
        <div className="flex flex-col w-full h-full min-h-fit p-4 xl:p-6 overflow-y-auto">
            {/* Back button */}
            <button
                type="button"
                onClick={() => navigate(-1)}
                className="mb-4 w-fit flex items-center gap-2 px-4 py-2 rounded-xl bg-black/10 hover:bg-black/20 text-xl font-semibold transition">
                <img
                    src="/icons/angle-left.svg"
                    alt="Nazaj"
                    className="w-6 h-6"
                />
                Nazaj
            </button>
            <h1 className="text-2xl xl:text-5xl mb-4 font-bold md:w-96 text-left">
                Uredi dogodek
            </h1>
            <form
                onSubmit={handleSubmit(onSubmit)}
                className="flex flex-col gap-4 w-full">
                <div className="flex flex-col gap-0.5">
                    <label className="font-semibold text-xl">Naslov</label>
                    <input
                        {...register("title", {
                            required: "Naslov je obvezen",
                        })}
                        type="text"
                        className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"
                    />
                    <div className="text-error h-2">
                        {errors.title && <>{errors.title.message}</>}
                    </div>
                </div>

                <div className="flex flex-col gap-0.5">
                    <label className="font-semibold text-xl">Opis</label>
                    <input
                        {...register("description", {})}
                        type="textarea"
                        className="bg-black/10 p-3 text-xl  rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"
                    />
                    <div className="text-error h-2">
                        {errors.description && (
                            <>{errors.description.message}</>
                        )}
                    </div>
                </div>

                <div className="flex flex-col gap-0.5">
                    <label className="font-semibold text-xl">Začetek</label>
                    <div>
                        <DateTimePicker
                            onChange={changeStartTime}
                            value={startTime}
                            format="d-MM-y H:mm"
                            className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"
                        />
                    </div>
                    <div className="text-error h-2">
                        {errors.starttime && <>{errors.starttime.message}</>}
                    </div>
                </div>

                <div className="flex flex-col gap-0.5">
                    <label className="font-semibold text-xl">Zaključek</label>
                    <div>
                        <DateTimePicker
                            onChange={changeEndTime}
                            value={endTime}
                            format="d-MM-y H:mm"
                            className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"
                        />
                    </div>
                    <div className="text-error h-2">
                        {errors.endtime && <>{errors.endtime.message}</>}
                    </div>
                </div>

                <div className="flex flex-col gap-0.5">
                    <label className="font-semibold text-xl">Lokacija</label>
                    <input
                        type="text"
                        className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"
                        value={selectedLocation ? selectedLocation.info : ""}
                        readOnly
                    />
                    <SelectMap
                        onLocationSelect={setSelectedLocation}
                        selectedLocation={selectedLocation}
                    />
                    <div className="text-error h-2">
                        {errors.password && <>{errors.password.message}</>}
                    </div>
                </div>

                <div className="flex flex-col gap-0.5">
                    <label className="font-semibold text-xl">Oznaka</label>
                    <select
                        className="bg-black/10 p-3 text-xl rounded-2xl shadow-md border-black/20 border-4"
                        value={tag}
                        onChange={(e) => setTag(e.target.value)}>
                        {tagOptions.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="flex flex-col gap-1 w-fit">
                    <label className="radio-label">
                        <input
                            type="radio"
                            {...register("public", { required: true })}
                            value="true"
                            className="radio-custom bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 mr-4"
                        />
                        Javno
                    </label>
                    <label className="radio-label">
                        <input
                            type="radio"
                            {...register("public", { required: true })}
                            value="false"
                            className="radio-custom bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4 mr-4"
                        />
                        Privat
                    </label>
                    <div className="text-error h-5">
                        {errors.public && <>{errors.public.message}</>}
                    </div>
                </div>

                <div className="w-32">
                    <button type="submit" className="btn-nav">
                        Shrani
                    </button>
                </div>
                <div className="text-error h-2 font-semibold w-full text-center">
                    {errors.root && <>{errors.root.message}</>}
                </div>
            </form>
        </div>
    );
}
