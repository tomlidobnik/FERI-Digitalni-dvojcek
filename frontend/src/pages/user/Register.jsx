import { Link } from "react-router-dom";
import { useForm } from "react-hook-form";

export default function Register() {
    const { 
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm();

    const onSubmit = async (data) => {
        try{
            console.log(data);
        }catch (error) {
            setError("root", {
                message: "Napaka pri registraciji uporabnika",
            });
        }
    }

    return (
        <div className="flex flex-col md:items-center md:justify-center h-screen select-none bg-tertiary text-text min-h-fit">
            <div className="flex flex-col  p-8 bg-primary md:rounded-4xl md:h-fit h-screen md:shadow-2xl">
                <h1 className="text-4xl mb-4 font-bold md:w-96 w-full text-left">Registracija</h1>
                <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 w-full">
                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">Uporabniško ime</label>
                        <input {...register("username", {
                            required: "Uporabniško ime je obvezno",
                        })} type="text" className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        {errors.username && <span className="text-error">{errors.username.message}</span>}
                    </div>

                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">Ime</label>
                        <input {...register("firstname", {
                            required: "Ime je obvezno",
                        })} type="text" className="bg-black/10 p-3 text-xl  rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        {errors.firstname && <span className="text-error">{errors.firstname.message}</span>}
                    </div>

                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">Priimek</label>
                        <input {...register("secondname",{
                            required: "Priimek je obvezno",
                        })} type="text" className="bg-black/10 p-3 text-xl  rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        {errors.secondname && <span className="text-error">{errors.secondname.message}</span>}
                    </div>

                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">E-naslov</label>
                        <input {...register("email",{
                            required: "E-naslov je obvezn",
                            pattern: {
                                value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                                message: "E-naslov ni veljaven"
                            }
                        })} type="text" className="bg-black/10 p-3 text-xl  rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        {errors.email && <span className="text-error">{errors.email.message}</span>}
                    </div>

                    <div className="flex flex-col gap-0.5">
                        <label className="font-semibold text-xl">Geslo</label>
                        <input {...register("password",{
                            required: "Geslo je obvezno",
                            minLength: {
                                value: 8,
                                message: "Geslo mora imeti vsaj 8 znakov"
                            },
                        })} type="password"className="bg-black/10 p-3 text-xl rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        {errors.password && <span className="text-error">{errors.password.message}</span>}
                    </div>

                    <div className="flex flex-col gap-1">
                        <label className="font-semibold text-xl">Potrditev gesla</label>
                        <input {...register("passwordAgain", {
                            required: "Ponovno vnesite geslo",
                            validate: (value) => {
                                if (value !== document.querySelector('input[name="password"]').value) {
                                    return "Geslo se ne ujema";
                                }
                            }
                        })} type="password" className="bg-black/10 p-3 text-xl  rounded-2xl shadow-md focus:border-tertiary focus:outline-tertiary focus:outline-0 border-black/20 border-4"/>
                        {errors.passwordAgain && <span className="text-error">{errors.passwordAgain.message}</span>}
                    </div>

                    <button type="submit" className="bg-tertiary text-text font-semibold text-2xl rounded-2xl hover:bg-quaternary active:bg-tertiary p-3 mt-1" >Nadaljuj</button>
                    <div className="w-full text-center text-text md:mb-0 mb-3">
                        Že imaš račun? 
                        <Link to="/login" className="font-semibold"> Prijavi se.</Link>
                    </div>

                    {errors.root && <span className="text-error">{errors.root.message}</span>}
                </form>
            </div>
        </div>
    );
}