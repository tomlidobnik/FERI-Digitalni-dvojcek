FROM rust:1.86-bullseye AS builder

RUN apt-get update && apt-get install -y libpq-dev pkg-config ca-certificates

WORKDIR /usr/src/backend

COPY . .

RUN cargo build --release --target-dir /tmp/build

FROM debian:bullseye-slim

RUN apt-get update && apt-get install -y libpq5 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=builder /tmp/build/release/backend /usr/local/bin/backend

COPY .env /app/.env

WORKDIR /app

EXPOSE 8000

CMD ["backend"]

