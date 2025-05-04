#!/bin/bash
set -e

echo "🔨 Building backend..."
docker build --platform linux/amd64 -t backend:prod ./backend

echo "🔨 Building frontend..."
docker build --platform linux/amd64 -t frontend:prod ./frontend

echo "🏷 Tagging images..."
docker tag backend:prod tomlidobnik/backend:prod
docker tag frontend:prod tomlidobnik/frontend:prod

echo "🚀 Pushing to Docker Hub..."
docker push tomlidobnik/backend:prod
docker push tomlidobnik/frontend:prod

echo "✅ Done!"

