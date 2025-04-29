#!/bin/bash

docker build -t backend:dev ./backend

docker-slim build --target=backend:dev

