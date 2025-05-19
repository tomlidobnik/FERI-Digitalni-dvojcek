import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { ErrorBoundary } from 'react-error-boundary';
import Cookies from 'js-cookie';

import './index.css';
import router from './router.jsx'; // Import router from the new file
import ErrorPage from './pages/ErrorPage.jsx'; // Import the ErrorPage component
import { Provider } from 'react-redux';
import store from './state/store.js'; // Import your Redux store

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <ErrorBoundary FallbackComponent = {ErrorPage}>
            <Provider store={store}>
                <RouterProvider router={router} />
            </Provider>
        </ErrorBoundary>
    </StrictMode>,
);
