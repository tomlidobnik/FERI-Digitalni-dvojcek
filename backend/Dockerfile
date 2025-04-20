FROM rust:latest

WORKDIR /usr/src/backend

RUN cargo install cargo-watch

COPY Cargo.toml Cargo.lock ./

RUN mkdir src

COPY . .

CMD ["cargo", "watch", "-x", "run"]

