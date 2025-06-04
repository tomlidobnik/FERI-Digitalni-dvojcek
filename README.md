# Copycats

## 1. Projektne specifikacije

### Namen projekta

Copycats je sodobna spletna aplikacija, namenjena študentom ter organizatorjem dogodkov. Omogoča ustvarjanje, upravljanje in vizualizacijo dogodkov, lokacij ter komunikacijo med uporabniki v realnem času. Glavne skupine uporabnikov so:

-   Študenti (za spremljanje dogodkov, komunikacijo, iskanje lokacij)
-   Organizatorji dogodkov (za ustvarjanje in upravljanje dogodkov, obveščanje udeležencev)
-   Administratorji (za nadzor in vzdrževanje sistema)

### Opis rešitve

Aplikacija omogoča:

-   Prijavo in registracijo uporabnikov
-   Upravljanje dogodkov (ustvarjanje, urejanje, brisanje, vizualizacija na zemljevidu)
-   Klepet med uporabniki (ena-na-ena in skupinski klepet po dogodkih)
-   Upravljanje prijateljstev in pošiljanje prošenj
-   Upravljanje in vizualizacijo lokacij (točke, območja)

### Funkcionalne zahteve

-   Prijava/registracija uporabnika (JWT avtentikacija)
-   Ustvarjanje, urejanje, brisanje dogodkov
-   Prikaz dogodkov na interaktivnem zemljevidu (Leaflet)
-   Klepet (dogodki, prijatelji) v realnem času (WebSocket)
-   Upravljanje prijateljev (pošiljanje, sprejemanje, zavračanje prošenj)
-   Upravljanje lokacij (točke, območja, shranjevanje, prikaz)
-   Obvestila in animacije
-   Podpora za večjezičnost (slovenščina)
-   Sistem mora delovati v Docker okolju (PostgreSQL, Rust backend, React frontend)

## 2. Navodila za namestitev in prijavo v sistem

### Namestitev (lokalno, Docker)

1. Kloniraj repozitorij:
    ```sh
    git clone https://github.com/tomlidobnik/FERI-Digitalni-dvojcek
    cd FERI-Digitalni-dvojcek
    ```
2. Ustvari certifikate (za https):
    ```sh
    mkcert -install
    mkcert -key-file backend/key.pem -cert-file backend/cert.pem localhost 127.0.0.1 ::1 # tukaj lahko dodate vaše ip naslove
    ```
3. Zaženi vse storitve (razvojni način):
    ```sh
    docker compose up --build db backend-dev frontend-dev
    ```
    Za produkcijo:
    ```sh
    bash build_backend.sh
    docker compose up --build db backend-prod frontend-prod
    ```
4. Aplikacija bo dostopna na [https://localhost:5173](https://localhost:5173) (dev) ali [https://localhost:3000](https://localhost:3000) (prod).

### Prijava v sistem

-   Ob prvem zagonu se registriraj z uporabniškim imenom in geslom.
-   Po prijavi lahko uporabljaš vse funkcionalnosti glede na vlogo.

## 3. Ključni primeri uporabe

### 1. Prijava in registracija

-   **Kaj želi uporabnik:** Prijaviti se ali ustvariti nov račun.
-   **Koraki:**
    1. Odpri aplikacijo.
    2. Klikni "Registracija" ali "Prijava".
    3. Vnesi podatke in potrdi.
-   **Rezultat:** Uporabnik je prijavljen in preusmerjen na nadzorno ploščo.

### 2. Ustvarjanje dogodka

-   **Kaj želi uporabnik:** Ustvariti nov dogodek.
-   **Koraki:**
    1. Prijavi se.
    2. Izberi "Dogodki" > "Ustvari dogodek".
    3. Izpolni podatke in shrani.
-   **Rezultat:** Dogodek je viden na seznamu in zemljevidu.

### 3. Klepet na dogodku

-   **Kaj želi uporabnik:** Klepetati z udeleženci dogodka.
-   **Koraki:**
    1. Prijavi se.
    2. Izberi dogodek in odpri klepet.
    3. Pošlji sporočilo.
-   **Rezultat:** Sporočilo je vidno vsem udeležencem dogodka v realnem času.

### 4. Dodajanje prijatelja in zasebni klepet

-   **Kaj želi uporabnik:** Dodati prijatelja in klepetati zasebno.
-   **Koraki:**
    1. Poišči uporabnika v seznamu.
    2. Pošlji prošnjo za prijateljstvo.
    3. Po potrditvi odpri zasebni klepet.
-   **Rezultat:** Uporabnika sta prijatelja in lahko klepetata.

### 5. Ustvarjanje in shranjevanje lokacije

-   **Kaj želi uporabnik:** Shraniti novo točko ali območje na zemljevidu.
-   **Koraki:**
    1. Prijavi se.
    2. Izberi "Zemljevid".
    3. Uporabi orodja za risanje in shrani lokacijo.
-   **Rezultat:** Lokacija je shranjena in vidna na zemljevidu.

## 4. Dokumentacija izvedenih lastnosti (features)

### Prijava in registracija

-   **Namen:** Omogočiti varen dostop do sistema.
-   **Implementacija:** JWT avtentikacija, obrazci v Reactu, preverjanje v backendu (Rust).

### Upravljanje dogodkov

-   **Namen:** Omogočiti ustvarjanje, urejanje in brisanje dogodkov.
-   **Implementacija:** REST API (Rust), obrazci in zemljevid (React, Leaflet), podatki v PostgreSQL.

### Klepet (dogodki/prijatelji)

-   **Namen:** Omogočiti komunikacijo med uporabniki v realnem času.
-   **Implementacija:** WebSocket (Rust backend, React frontend), ločen chat za dogodke in prijatelje.

### Upravljanje prijateljev

-   **Namen:** Omogočiti povezovanje uporabnikov.
-   **Implementacija:** Pošiljanje, sprejemanje, zavračanje prošenj prek REST API in WebSocket.

### Upravljanje lokacij

-   **Namen:** Omogočiti shranjevanje in prikaz točk/območij na zemljevidu.
-   **Implementacija:** Leaflet, risanje in shranjevanje prek REST API, prikaz s custom markerji in poligoni.

### Obvestila in animacije

-   **Namen:** Izboljšati uporabniško izkušnjo.
-   **Implementacija:** React animacije, Notification komponenta, tematski dizajn.

---

Za več informacij glej posamezne README.md datoteke v /backend in /frontend ter izvorno kodo.