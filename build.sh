#!/bin/bash
set -e

docker buildx create --use --name multi-arch-builder || docker buildx use multi-arch-builder

echo "$BACKEND_ENV_FILE" > ./backend/.env

echo "🔨 Building and pushing backend for linux/amd64..."
docker buildx build --platform linux/amd64 -t tomlidobnik/backend:prod -f backend/Dockerfile ./backend --push

echo "$FRONTEND_ENV_FILE" > ./frontend/.env

echo "🔨 Building and pushing frontend for linux/amd64..."
docker buildx build --platform linux/amd64 -t tomlidobnik/frontend:prod -f frontend/Dockerfile ./frontend --push

echo "✅ Build and push complete!"

