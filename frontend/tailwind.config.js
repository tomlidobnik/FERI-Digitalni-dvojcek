module.exports = {
    content: [
        "./src/**/*.{js,jsx,ts,tsx}",
        "./node_modules/flowbite/**/*.js", // Added for Flowbite if components are in node_modules
    ],
    theme: {
        extend: {
            colors: {
                primary: "var(--color-primary)",
                secondary: "var(--color-secondary)",
                tertiary: "var(--color-tertiary)",
                quaternary: "var(--color-quaternary)",
                error: "var(--color-error)",
                warning: "var(--color-warning)",
                "text-custom": "var(--color-text)",
                "text-clickable": "var(--color-text-clickable)",
                "text-hover": "var(--color-text-hover)",
                "text-active": "var(--color-text-active)",
                "text-disabled": "var(--color-text-disabled)",
            },
        },
    },
    plugins: [
        require("@tailwindcss/line-clamp"),
        require("flowbite/plugin"), // Added Flowbite plugin here as is standard
    ],
};
