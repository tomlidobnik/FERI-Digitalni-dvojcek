import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import Cookies from 'js-cookie';
import { useEffect } from "react";

export default function Login() {
    const navigate = useNavigate();

    const { 
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm();

    useEffect(() => { // preusmeritev uporabnika če je že prijavljen
        const token = Cookies.get("token");
        if (token) {
            navigate("/");
        }
    });

    const onSubmit = async (data) => {
        try{
            const userData = {
                username: data.username,
                password: data.password,
            };
            const response = await fetch(`https://${import.meta.env.VITE_API_URL}/api/user/token`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(userData),
            });
            if (!response.ok) {
                const errorData = await response.json();
                if (errorData.error) {
                    setError("root", {
                        message: errorData.error,
                    });
                } else {
                    setError("root", {
                        message: "Napaka pri prijavi uporabnika",
                    });
                }
                return;
            }else if (response.ok) {
                //console.log(await response.json());
                //console.log("User logged in successfully");
                const token = await response.text();
                const expirationDate = new Date(Date.now() + 30 * 60 * 1000);
                Cookies.set("token", token, { expires: expirationDate, secure: true });

                const storedToken = Cookies.get("token");
                if (storedToken) {
                    //console.log("Token successfully set:", storedToken);
                    navigate("/");
                } else {
                    setError("root", {
                        message: "Napaka pri nastavitvi piškotka",
                    });
                }
            }
            //console.log("User registered successfully");
        }catch (error) {
            setError("root", {
                message: "Napaka pri prijavi uporabnika",
            });
        }
    }

    return (
        <div className="flex flex-col md:items-center md:justify-center h-screen select-none bg-tertiary text-text min-h-fit">
            <div className="flex flex-col  p-8 bg-primary md:rounded-4xl md:h-fit h-screen md:shadow-2xl md:my-8">
                <h1 className="text-4xl mb-4 font-bold md:w-96 w-full text-left">Prijava</h1>
                <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 w-full">
                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">Uporabniško ime</label>
                        <input {...register("username", {
                            required: "Uporabniško ime je obvezno",
                        })} type="text" className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        <div className="text-error h-2">
                            {errors.username && <>{errors.username.message}</>}
                        </div>
                    </div>

                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">Geslo</label>
                        <input {...register("password",{
                            required: "Geslo je obvezno",
                        })} type="password"className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        <div className="text-error h-2">
                            {errors.password && <>{errors.password.message}</>}
                        </div>
                    </div>


                    <button type="submit" className="bg-tertiary text-text font-semibold text-2xl rounded-2xl hover:bg-quaternary active:bg-tertiary p-3 mt-1" >Prijavi se</button>
                    <div className="w-full text-center text-text md:mb-0">
                        Še nimaš računa? 
                        <Link to="/register" className="font-semibold"> Registriraj se.</Link>
                    </div>
                    <div className="text-error h-2 font-semibold w-full text-center">
                            {errors.root && <>{errors.root.message}</>}
                    </div>
                </form>
            </div>
        </div>
    );
}