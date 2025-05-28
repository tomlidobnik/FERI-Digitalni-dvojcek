import Cookies from "js-cookie";

const API_BASE_URL = `https://${import.meta.env.VITE_API_URL}/api`;

const baseFetchApi = async (endpoint, options = {}) => {
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
                errorData?.error ||
                `API request failed: ${response.status} ${response.statusText}`;

            const error = new Error(errorMessage);
            error.status = response.status;
            if (errorData) {
                error.data = errorData;
            }
            throw error;
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
            return text;
        }
    } catch (error) {
        console.error(`API call to ${endpoint} failed:`, error.message);
        throw error;
    }
};

const apiService = {
    get: (endpoint, options = {}) =>
        baseFetchApi(endpoint, { ...options, method: "GET" }),
    post: (endpoint, body, options = {}) =>
        baseFetchApi(endpoint, { ...options, method: "POST", body }),
    put: (endpoint, body, options = {}) =>
        baseFetchApi(endpoint, { ...options, method: "PUT", body }),
    delete: (endpoint, options = {}) =>
        baseFetchApi(endpoint, { ...options, method: "DELETE" }),
    patch: (endpoint, body, options = {}) =>
        baseFetchApi(endpoint, { ...options, method: "PATCH", body }),
};

export default apiService;

export { baseFetchApi as fetchApi };
