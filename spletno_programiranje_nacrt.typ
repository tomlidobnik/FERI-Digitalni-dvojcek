#set page(
  paper: "us-letter",
  numbering: "1",
)

#align(center)[
  #set align(horizon)
  #set text(18pt)

  = Načrt za spletno programiranje
  April 2025
]

#pagebreak()
#outline(
  title: [Kazalo],
)
#set heading(numbering: "1.1.1")
#pagebreak()

= Uporabljene tehnologije

- Rust kot jezik za API
- Axum framework za pisanje API
- Diesel.rs za ORM
- PostgreSQL za podatkovno bazo
- React za čelni del

= Opis podatkov, ki se bodo shranjevali/uporabljali

- Uporabniki in njegove informacije (uporabniško ime, profilna slika, povezave z drugimi uporabniki, ...)
- Lokacija (Točka na zemljevidu)
- Lokacija (Poligon na zemljevidu)
- Dogodki in lastnosti za dogodge

= Funkcionalnosti

- Registracija uporabnika
- Prijava uporabnika
- Seje
- Ustvarjanje dogodkov
- Pregled dogodkov (Pogled nadzorne plošče)
- Podrobnejši prikaz za posamezen dogodek (Pogled Nadzorne plošče)
- Prijava na dogodek
- Prikaz dogodkov na zemljevidu
- Filtrirano iskanje dogodkov

== Dodatne

- Klepetalnice (med uporabniki in skupne klepetalnice za posamezen dogodek)
- Uporabnikovo dodajanje lokacij (morajo biti preverjena in potrjena iz strani administarotja)

= Roki

== Sprint 1
18 Apr - 25 Apr
- Ustvarjanje izvajalnega okolja za spletno aplikacijo in vse ostale storitve.
- Implementacija sej z JWT tokeni
- Validacije gesel (API)
- Ustvarjanje in shranjevanje uporabnika (API)

== Sprint 2
25 Apr - 9 Maj
- Registracija uporabnika
- Prijava uporabnika
- Shranjevanje uporabnikov v podatkovno bazo
- Urejanje profila
- Nadzorna plošča (Dashboard)

== Sprint 3
12 Maj - 23 Maj
Naloge za SCRUM sprint 3 še niso bile določene.
