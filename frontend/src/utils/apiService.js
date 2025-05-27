import Cookies from "js-cookie";

const API_BASE_URL = `https://${import.meta.env.VITE_API_URL}/api`;

/**
 * A utility function for making API requests.
 * @param {string} endpoint - The API endpoint (e.g., '/location/list').
 * @param {object} options - Fetch options (method, body, headers, etc.).
 * @returns {Promise<any>} - A promise that resolves with the JSON response or null.
 * @throws {Error} - Throws an error if the request fails or the response is not ok.
 */
export const fetchApi = async (endpoint, options = {}) => {
    const token = Cookies.get("token");
    const defaultHeaders = {};

    if (token) {
        defaultHeaders["Authorization"] = `Bearer ${token}`;
    }

    if (
        options.body &&
        typeof options.body === "object" &&
        !(options.body instanceof FormData)
    ) {
        defaultHeaders["Content-Type"] = "application/json";
        options.body = JSON.stringify(options.body);
    }

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers,
        },
    };

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json();
            } catch (e) {}
            const errorMessage =
                errorData?.message ||
                `API request failed: ${response.status} ${response.statusText}`;
            throw new Error(errorMessage);
        }

        const text = await response.text();
        if (!text) {
            return null;
        }
        try {
            return JSON.parse(text);
        } catch (e) {
            console.warn(
                `Response from ${endpoint} was successful but not valid JSON. Content (truncated): ${text.substring(
                    0,
                    100
                )}`
            );
            return null;
        }
    } catch (error) {
        console.error(`API call to ${endpoint} failed:`, error);
        throw error;
    }
};
