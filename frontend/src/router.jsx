import { createBrowserRouter } from 'react-router-dom';
import App from './pages/home/App.jsx';
import Login from './pages/user/Login.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import { ErrorBoundary } from 'react-error-boundary';
import ErrorPage from './pages/ErrorPage.jsx'; // Import the ErrorPage component

// Routers
const router = createBrowserRouter([
    {
        path: '/',
        element: <App />,
        errorElement: <NotFoundPage />,
    },
    {
        path: '/login',
        element: (
            <ErrorBoundary FallbackComponent={ErrorPage}>
                <Login />
            </ErrorBoundary>
        ),
    },
]);

export default router;
