import { Link } from "react-router-dom";

function ErrorPage(error) {
    return(
        <div className="flex flex-col items-center justify-center h-screen select-none ">
            <div className="flex items-center flex-col justify-center mb-4 bg-primary p-16 rounded-4xl shadow-2xl">
                <h1 className="text-6xl sm:text-9xl font-extrabold text-error m-2">Napaka!</h1>
                <h2 className="text-2xl sm:text-4xl font-bold text-text">{error["error"].message}</h2>
                <Link
                    to="/"
                    className="mt-6 inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-black/20 text-text font-semibold text-xl sm:text-2xl shadow hover:bg-quaternary/80 transition group"
                >
                    <img
                        src="/icons/angle-left.svg"
                        className="w-6 h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:-translate-x-2"
                        alt="home"
                    />
                    Pojdi na domačo stran
                </Link>
            </div>
        </div>
    )
}

export default ErrorPage;