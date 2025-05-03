import { createBrowserRouter } from 'react-router-dom';
import App from './pages/home/App.jsx';
import Login from './pages/user/Login.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';

// Routers
const router = createBrowserRouter([
    {
        path: '/',
        element: <App />,
        errorElement: <NotFoundPage />,
    },
    {
        path: '/login',
        element: <Login />,
    }
]);

export default router;
