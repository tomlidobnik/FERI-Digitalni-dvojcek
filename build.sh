#!/bin/bash
set -e

docker buildx create --use --name multi-arch-builder || docker buildx use multi-arch-builder

echo "🔨 Building and pushing backend for linux/arm64..."
docker buildx build --platform linux/arm64 -t tomlidobnik/backend:prod -f backend/Dockerfile ./backend --push

echo "🔨 Building and pushing frontend for linux/arm64..."
if [ ! -f frontend/package-lock.json ]; then
  echo "package-lock.json not found, running npm install to generate it..."
  (cd frontend && npm install)
fi
docker buildx build --platform linux/arm64 -t tomlidobnik/frontend:prod -f frontend/Dockerfile ./frontend --push

echo "✅ Build and push complete!"

