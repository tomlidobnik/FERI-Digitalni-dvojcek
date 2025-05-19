import { createBrowserRouter } from "react-router-dom";
import App from "./pages/home/App.jsx";
import Login from "./pages/user/Login.jsx";
import NotFoundPage from "./pages/NotFoundPage.jsx";
import { ErrorBoundary } from "react-error-boundary";
import ErrorPage from "./pages/ErrorPage.jsx"; // Import the ErrorPage component
import Register from "./pages/user/Register.jsx";
import EventChat from "./components/Chat/EventChat.jsx";
import FriendChat from "./components/Chat/FriendChat.jsx";
import LocationsMap from "./pages/map/LocationsMap.jsx";
import EventsMap from "./pages/map/EventsMap.jsx";
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
        path: "/event_chat",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <EventChat />
            </ErrorBoundary>
        ),
    },
    {
        path: "/friend_chat",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <FriendChat />
            </ErrorBoundary>
        ),
    },
    {
        path: "/locations_map",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <LocationsMap />
            </ErrorBoundary>
        ),
    },
    {
        path: "/events_map",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <EventsMap />
            </ErrorBoundary>
        ),
    },
]);

export default router;
