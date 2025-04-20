# Use a Node.js base image
FROM node:23-slim

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock)
COPY package*.json ./

# Install the dependencies
RUN npm install

# Copy the rest of the project files
COPY . .

# Expose port 5173 for Vite (default port)
EXPOSE 5173

# Command to run Vite in development mode (bound to 0.0.0.0)
CMD ["npm", "run", "dev"]

