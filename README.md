# FERI-Digitalni-dvojcek

```sh
# PROD
bash build_backend.sh
docker-compose up --build db backend-prod frontend-prod

#DEV
docker-compose up --build db backend-dev frontend-dev

docker compose down
```

Certifikati:

```sh
mkcert -install
mkcert -key-file key.pem -cert-file cert.pem NAME HOST ::1
```

## Excelidraw

[Shema](https://excalidraw.com/#json=1OcuNvEky7hKNb6fpxw7c,p-O-1NmXBumjLYPRgYIwzA)

[Icons](https://www.flaticon.com/icon-fonts-most-downloaded?weight=regular&type=uicon)