import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { ErrorBoundary } from 'react-error-boundary';

import './index.css';
import router from './router.jsx'; // Import router from the new file
import ErrorPage from './pages/ErrorPage.jsx'; // Import the ErrorPage component

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <ErrorBoundary FallbackComponent = {ErrorPage}>
            <RouterProvider router={router} />
        </ErrorBoundary>
    </StrictMode>,
);
