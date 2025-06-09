# Dokumentacija

## Prva stran

**Ime projekta:** Copycats

**Člani skupine:**

-   Tom Li Dobnik
-   Alen Kolmanič
-   Anej Bregant

**Povezava do repozitorija:**
[https://github.com/tomlidobnik/FERI-Digitalni-dvojcek](https://github.com/tomlidobnik/FERI-Digitalni-dvojcek)

```mermaid
gantt
title Ganttov diagram
dateFormat  YYYY-MM-DD
section Načrtovanje
Analiza         :a1, 2025-04-21, 7d
Zagon        :a2, 2025-04-21, 7d
section Razvoj
Zaledni razvoj         :b1, after a2, 20d
Čelni razvoj        :b2, after a2, 30d
Namizna aplikacija :b3, 2025-05-15, 17d
section Zaključek
Dokumentacija          :c1, 2025-06-01, 8d
```

## Primeri uporabe

**Opis problema:**
Copycats omogoča uporabnikom digitalno povezovanje, organizacijo dogodkov, komunikacijo in deljenje lokacij. Problem rešuje digitalno povezovanje in sodelovanje v realnem času.

### Sekvenčni diagrami za ključne funkcionalnosti

#### 1. Prijava uporabnika

```mermaid
sequenceDiagram
    participant Uporabnik
    participant Proxy
    participant Frontend
    participant Backend
    participant DB
    Uporabnik->>Proxy: GET /login
    Proxy->>Frontend: Streže React aplikacijo
    Frontend->>Proxy: Prikaže login formo
    Proxy->>Uporabnik: Prikaže login formo
    Uporabnik->>Proxy: POST /api/user/token (podatki za prijavo)
    Proxy->>Backend: Posreduje zahtevo
    Backend->>DB: Preveri uporabnika
    DB->>Backend: Rezultat preverjanja
    Backend->>Proxy: JWT ali napaka
    Proxy->>Uporabnik: Prijava uspešna ali napaka
```

#### 2. Ustvarjanje dogodka

```mermaid
sequenceDiagram
    participant Uporabnik
    participant Proxy
    participant Frontend
    participant Backend
    participant DB
    Uporabnik->>Proxy: GET /events/create
    Proxy->>Frontend: Streže React aplikacijo
    Frontend->>Proxy: Prikaže formo za dogodek
    Proxy->>Uporabnik: Prikaže formo za dogodek
    Uporabnik->>Proxy: POST /api/event/create (podatki dogodka)
    Proxy->>Backend: Posreduje zahtevo
    Backend->>DB: Shrani dogodek
    DB->>Backend: OK ali napaka
    Backend->>Proxy: Potrditev ali napaka
    Proxy->>Uporabnik: Prikaz rezultata
```

#### 3. Pošiljanje sporočila prek WebSocket

```mermaid
sequenceDiagram
    participant Uporabnik
    participant Proxy
    participant Frontend
    participant Backend
    participant DB
    Uporabnik->>Proxy: GET /event/{id}/chat
    Proxy->>Frontend: Streže React aplikacijo
    Frontend->>Proxy: Prikaže chat UI
    Proxy->>Uporabnik: Prikaže chat UI
    Uporabnik->>Proxy: WebSocket povezava (ws://...)
    Proxy->>Backend: Posreduje WebSocket povezavo
    Uporabnik->>Proxy: Pošlje sporočilo prek WebSocket
    Proxy->>Backend: Posreduje sporočilo
    Backend->>DB: Shrani sporočilo
    DB->>Backend: OK
    Backend->>Proxy: Potrditev/novo sporočilo
    Proxy->>Frontend: Novo sporočilo
    Frontend->>Uporabnik: Prikaz novega sporočila
```

## Arhitektura programske rešitve

### Arhitekturni diagram

![arhitektura](arhitektura.png)

**Uporabljene tehnologije:**

-   Programska jezika: Rust (backend), JavaScript/React (frontend)
-   Podatkovna baza: PostgreSQL
-   Komunikacijski protokoli: HTTP/HTTPS, WebSocket
-   Spletni strežnik: Axum (Rust)
-   Prevajalnik: Rustc, Babel (JS)
-   Docker (kontejnerizacija, razvoj in produkcija)
-   Docker Compose (orkestracija več storitev)
-   Nginx (reverse proxy, SSL terminacija)
-   GitHub Actions (CI/CD)
-   FastAPI (webhook za avtomatski deploy)
-   Typst (generiranje PDF dokumentacije)
-   bcrypt (hashiranje gesel)
-   SlowAPI (rate limiting za webhook)
-   GitHub Secrets, Docker Secrets (upravljanje občutljivih podatkov)

**Knjižnice in API:**

-   Diesel (ORM za Rust, delo z bazo)
-   Axum (Rust web framework)
-   React (frontend)
-   JWT (avtentikacija)
-   Tower HTTP (statika, CORS)
-   serde, serde_json (serializacija/deserializacija v Rustu)
-   tokio (asinkrono programiranje v Rustu)
-   tower (middleware za Axum)
-   multer (file upload v Axum)
-   dotenv (upravljanje konfiguracijskih spremenljivk)
-   react-router-dom (navigacija v Reactu)
-   tailwindcss (stiliranje frontend aplikacije)
-   axios (HTTP odjemalec v Reactu)
-   formik, yup (upravljanje in validacija obrazcev v Reactu)
-   react-dropzone (drag & drop upload slik)
-   react-toastify (obvestila v Reactu)
-   ws (WebSocket podpora v Rustu in Reactu)
-   pg (PostgreSQL odjemalec)
-   Zakaj: hitrost, varnost, moderna arhitektura

**Komunikacija:**

-   Čelni del (port 3000)
-   REST API (port 8000)
-   WebSocket (port 8888)
-   HTTPS (TLS certifikati)

### Razredni diagram (Rust)

```mermaid
classDiagram
    class User {
        +i32 id
        +String username
        +String firstname
        +String lastname
        +String email
        +String password
    }
    class Event {
        +i32 id
        +i32 user_fk
        +String title
        +String description
        +NaiveDateTime start_date
        +NaiveDateTime end_date
        +i32 location_fk
        +bool public
        +String tag
    }
    class Friend {
        +i32 id
        +i32 user1_fk
        +i32 user2_fk
        +i32 status
    }
    class EventUser {
        +i32 event_id
        +i32 user_id
    }
    class FriendChatMessage {
        +i32 id
        +i32 user_fk
        +String message
        +Timestamp created_at
        +i32 friend_fk
    }
    class ChatMessage {
        +i32 id
        +i32 user_fk
        +String message
        +Timestamp created_at
        +i32 event_fk
    }
    class Location {
        +i32 id
        +String info
        +f32 longitude // nullable
        +f32 latitude // nullable
        +i32 location_outline_fk // nullable
    }
    class LocationOutline {
        +i32 id
        +Jsonb points
    }
    User "1" --o "*" Friend
    User "1" --o "*" Event
    User "1" --o "*" EventUser
    Event "1" --o "*" EventUser
    Event "1" --o "*" Friend
    User "1" --o "*" ChatMessage
    Event "1" --o "*" ChatMessage
    Friend "1" --o "*" FriendChatMessage
    User "1" --o "*" FriendChatMessage
    Location "1" --o "*" Event
    LocationOutline "1" --o "1" Location
```

### Razredni diagram (Kotlin)

```mermaid
classDiagram
    class User {
        +Int id
        +String username
        +String firstname
        +String lastname
        +String email
        +String password
    }

    class Event {
        +Int id
        +String title
        +String description
        +String start_Date
        +String end_Date
        +Int location_fk
        +Boolean public
        +String tag
    }

    class Location {
        +Int id
        +String info
        +Double? longitude
        +Double? latitude
        +Int? location_outline_fk
    }

    class LocationOutline {
        +Int id
        +String info
        +List~Coordinate~ points
    }

    User "1" --o "*" Event
    Location "1" --o "*" Event
    LocationOutline "1" --o "1" Location
```

## DevOps (CI/CD)

### Potek dela

```mermaid
flowchart TD
    commit["Push na GitHub (develop branch)"]
    github["GitHub Actions (deploy.yml)"]
    build["Build & Test (build.sh, Docker)"]
    docker["Docker Image Push"]
    webhook["Webhook na strežniku"]
    deploy["Deploy na strežnik (docker-compose pull & up)"]
    commit-->github-->build-->docker-->webhook-->deploy
```

-   Ob vsakem pushu na vejo `develop` se sproži GitHub Actions workflow (`deploy.yml`).
-   Koda se preveri, zgradi in zapakira v Docker image.
-   Docker image se potisne na Docker Hub.
-   Webhook na strežniku sproži avtomatski deploy (docker-compose pull & up).
-   CI/CD zagotavlja avtomatsko, varno in ponovljivo nameščanje aplikacije.

## Varnost programske rešitve

-   Uporaba požarnega zidu
-   Omejitve uporabnikov (JWT, preverjanje vlog, preverjanje pravic na API endpointih)
-   Varnostne vloge: navaden uporabnik, admin
-   Varovanje podatkov: HTTPS, hashirana gesla (bcrypt), CORS, preverjanje vhodnih podatkov
-   Statika in slike servirane iz ločenega direktorija
-   Rate limiting na webhook endpointu (SlowAPI limiter v Python FastAPI implementaciji)
-   Preverjanje HMAC podpisa na webhook endpointu (varnost pred nepooblaščenimi deployi)
-   Uporaba Docker secretov in GitHub secretov za shranjevanje občutljivih podatkov (gesla, ključi)
-   Ločena okolja za razvoj in produkcijo (različni Dockerfile, različne konfiguracije)
-   Pravilna nastavitev CORS politike na backendu
-   Nginx kot reverse proxy za dodatno plast varnosti in SSL terminacijo
-   Pravilno nastavljeni Docker volumni (podatki baze niso v kontejnerju)
-   Pravilno nastavljene pravice na strežniku (uporabniški dostop, SSH ključi, port forwarding)
-   Samodejno posodabljanje aplikacije le ob preverjenem podpisu (webhook)
