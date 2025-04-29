FROM debian:bullseye-slim AS builder-base
RUN apt-get update && apt-get install -y curl build-essential libpq-dev pkg-config ca-certificates

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

WORKDIR /usr/src/backend
COPY . .

RUN cargo build --release

FROM debian:bullseye-slim
RUN apt-get update && apt-get install -y libpq5 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=builder-base /usr/src/backend/target/release/backend /usr/local/bin/backend
COPY .env .env

EXPOSE 8000
CMD ["backend"]

