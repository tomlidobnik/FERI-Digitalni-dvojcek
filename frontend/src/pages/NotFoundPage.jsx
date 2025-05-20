import { Link } from "react-router-dom";

function NotFoundPage() {
    return(
        <div className="flex flex-col items-center justify-center h-screen select-none ">
            <div className="flex items-center flex-col justify-center mb-4 bg-primary p-16 rounded-4xl shadow-2xl">
                <h1 className="text-6xl sm:text-9xl font-extrabold text-error">404</h1>
                <h2 className="text-2xl sm:text-4xl font-bold text-text">Stran ni bila najdena</h2>
                <Link
                    to="/"
                    className="mt-6 inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-black/20 text-text font-semibold text-xl sm:text-2xl shadow hover:bg-quaternary/80 transition"
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="w-6 h-6"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        strokeWidth={2}
                    >
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                    </svg>
                    Pojdi na domačo stran
                </Link>
            </div>

        </div>
    )
}

export default NotFoundPage;