```sh
docker build -t backend .
docker-slim build --target backend
docker run -p 8000:8000 docker.slim
```
