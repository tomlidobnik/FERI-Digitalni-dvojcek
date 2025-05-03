import { Link } from "react-router-dom";

function ErrorPage(error) {
    return(
        <div className="flex flex-col items-center justify-center h-screen select-none">
            <h1 className="text-6xl sm:text-9xl font-extrabold text-error m-2">Napaka!</h1>
            <h2 className="text-2xl sm:text-4xl font-bold text-text">{error["error"].message}</h2>
            <Link to="/" className="text-xl sm:text-2xl font-semibold">Pojdi na domačo stran</Link>
        </div>
    )
}

export default ErrorPage;