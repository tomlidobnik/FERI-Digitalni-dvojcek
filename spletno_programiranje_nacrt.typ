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

- Uporabniki in njegove informacije
- Lokacija (Točka na zemljevidu)
- Lokacija (Poligon na zemljevidu)
- Dogodki in lastnosti za dogodge

= Funkcionalnosti

- Registracija uporabnika
- Prijava uporabnika
- Seje
- Ustvarjanje dogodkov
- Pregled dogodkov
- Prijava na dogodek
- Prikaz dogodkov na zemljevidu
- Filtrirano iskanje dogodkov

== Dodatne

- Uporabnikovo dodajanje lokacij
