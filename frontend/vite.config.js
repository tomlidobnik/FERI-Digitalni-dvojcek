import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
    plugins: [react(), tailwindcss()],
    server: {
        host: "0.0.0.0",
        port: 5173,
        open: true,
        watch: {
            usePolling: true,
            interval: 100,
        },
    },
    build: {
        outDir: "dist",
        minify: true,
    },
});
