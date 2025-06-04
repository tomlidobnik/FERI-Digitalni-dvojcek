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
import MainPage from "./pages/MainPage.jsx";


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
        path: "/chat",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <FriendChat />
            </ErrorBoundary>
        ),
    },
    {
        path: "/friends",
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
        path: "/map",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
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
        path: "/events",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
            </ErrorBoundary>
        ),
    },
    {
        path: "/events/add",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
            </ErrorBoundary>
        ),
    },
        {
        path: "/event/edit",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
            </ErrorBoundary>
        ),
    },
    {
        path: "/event/:id",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
            </ErrorBoundary>
        ),
    },
    {
        path: "/profile",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
            </ErrorBoundary>
        ),
    },
    {
        path: "/profile/:id",
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <MainPage />
            </ErrorBoundary>
        ),
    },
]);

export default router;
