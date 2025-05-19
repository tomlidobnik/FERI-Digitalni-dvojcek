import {
    createBrowserRouter,
    UNSAFE_getTurboStreamSingleFetchDataStrategy,
} from "react-router-dom";
import Dashbord from "./pages/home/Dashbord.jsx";
import App from "./pages/App.jsx";
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
import ListUsers from "./components/Chat/ListUsers.jsx";

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
        path: "/add_friends",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <ListUsers />
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
    {
        path: "/home",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Dashbord />
            </ErrorBoundary>
        ),
    },
    {
        path: "/home",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Dashbord />
            </ErrorBoundary>
        ),
    },
]);

export default router;
