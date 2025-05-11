import { createBrowserRouter } from "react-router-dom";
import App from "./pages/home/App.jsx";
import Login from "./pages/user/Login.jsx";
import NotFoundPage from "./pages/NotFoundPage.jsx";
import { ErrorBoundary } from "react-error-boundary";
import ErrorPage from "./pages/ErrorPage.jsx"; // Import the ErrorPage component
import Register from "./pages/user/Register.jsx";
import ChatBox from "./components/Chat/ChatBox";
import Map from "./pages/map/Map.jsx";
import Logout from "./pages/user/Logout.jsx";

// Routers
const router = createBrowserRouter([
    {
        path: "/",
        element: <App />,
        errorElement: <NotFoundPage />,
    },
    {
        path: "/login",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Login />
            </ErrorBoundary>
        ),
    },
    {
        path: "/register",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Register />
            </ErrorBoundary>
        ),
    },
    {
        path: "/logout",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Logout />
            </ErrorBoundary>
        ),
    },
    {
        path: "/chat",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <ChatBox />
            </ErrorBoundary>
        ),
    },
    {
        path: "/map",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Map />
            </ErrorBoundary>
        ),
    },
]);

export default router;
