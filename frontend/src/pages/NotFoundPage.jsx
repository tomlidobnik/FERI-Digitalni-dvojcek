import { Link } from "react-router-dom";

function NotFoundPage() {
    return(
        <div>
            404 Not Found
            <Link to="/">Go to Home </Link>
        </div>
    )
}

export default NotFoundPage;