#set page(
  paper: "us-letter",
  numbering: "1",
)

#align(center)[
  #set align(horizon)
  #set text(28pt)

  #block[Poročilo]

  #set text(18pt)
  #block[Maj 2025]
]

#pagebreak()
#outline(
  title: [Kazalo],
)
#set heading(numbering: "1.1.1")
#pagebreak()

= Uvod
Za predmet prevajanje programskih jezikov smo se odločili, da bomo implementirali prevajalnik za naš lastni programski jezik. Ta jezik bo namenjen opisovanju mestne infratrukture in njihovih lastnosti. Naš cilj je ustvariti preprost prevajalnik, ki bo sposoben obdelati osnovne geometrijske konstrukte in jih pretvoriti v GeoJSON format.

= Osnova
Zahtevano je, da lahko z uporabo vašega jezika opišete geometrijske strukture, točke in ceste v mestu. Torej je potrebno podpreti polilinije (polyline) in poligone (polygon). Na primer, majhen del mesta bi lahko opisali takole:

```
let @p = (1,1.12);
let @q = (2,2);

city ["Maribor"]{
    road["Ptujska cesta"]{
        polyline[(1,2.5),(3.1,4),(5,6)];
    };
    building["FERI"]{
        polygon[(1,1),(1,2),(2,2.7),(2,1),(1,1)];
    };
    area["Igrišče"]{
        polygon[(2,3),(1,1),(5,7),(2,3)];
    };
    lake["Jezero Bled"]{
        circle[(4,3),2.8];
    };
    park["Park Maribor"]{
        circle[(5,9),2];
    };
    road["Ljubljanska cesta"]{
        polyline[$p,$q,$p+$q];
    };
}

?{[(1,1),$p,$q,(3,4)],[(1,1),3]};
let @r = (fst(p),snd(q));
```
== Konstrukti
Uporabljen jezik vsebuje naslednje konstrukte.

=== Enota !
Naša nevtralna enota je:
`null`

=== Realna števila
Za vrednosti imamo realna števila:
`1.0`, `2.5`, `-3.14`, `0.0`

=== Nizi
Za nize uporabljamo dvojne narekovaje, najdemo jih lahko v oglatih oklepajih:
`"Ptujska cesta"`, `"FERI"`, `"Park"`

=== Koordinate
S koordinatami lahko predstavimo lokacije na zemljevidu, v tem primeru je prva komponenta longituda, druga komponenta pa je latituda.
`(1.0,2.5)`, `(3.1,4)`, `(5,6)`

=== Bloki
Blok je sestavljen iz imena objekta, ki ga želimo definirati, definiramo jih lahko s "polygon", "polyline" in "circle". Blok se zaključi z okroglimi oklepaji.
==== City
Blok "City" je osrednji blok, ki ga uporabljamo za definiranje mesta. Vsebuje lahko druge bloke, kot so ceste, stavbe, območja, parki in jezera.
```
    city["IME"]{
        BLOKI
    };
```

==== Road
"Road" je blok, ki ga uporabljamo za definiranje ceste. Vsebuje lahko ukaze za izris, ki izrišejo črte.
```
    road["IME"]{
        COMMANDS
    };
```
==== Building
"Building" je blok, ki ga uporabljamo za definiranje stavb. Vsebuje lahko ukaze za izris stavbe.
```
    building["IME"]{
        COMMANDS
    };
```
==== Area
"Area" je blok, ki ga uporabljamo za definiranje območij. Vsebuje lahko ukaze za izris območij.
```
    area["IME"]{
        COMMANDS
    };
```

=== Ukazi
Bloki vsebujejo ukaze, ki jih lahko uporabimo za izris geometrijskih oblik. Ukazi so lahko "polyline", "polygon" in "circle".
==== Polyline
"Polyline" je ukaz, ki ga uporabljamo za izris polilinij. Vsebuje lahko koordinate za točke, med katerimi so izrisane črte. Končna in začetna točki sta lahko poljubni.
```
  polyline[TOČKA,TOČKA,TOČKA];
```
==== Polygon
"Polygon" je ukaz, ki ga uporabljamo za izris poligonov. Vsebuje lahko koordinate za točke, med katerimi so izrisane črte. Poligon se mora zaključi z začetno točko.
```
  polygon[TOČKA1,TOČKA2,TOČKA3,TOČKA1];
```
==== Circle
"Circle" je ukaz, ki ga uporabljamo za izris krogov. Vsebuje lahko koordinato za središče in polmer kroga.
```
  circle[TOČKA, RADIJ];
```
= Nadgradja
Jezik smo nadgradili z validacijo, dodatnimi elementi, spremenljivkami, izjavami in povpraševanji.

== Validacija
Validirali bomo poligone pri čemer bomo preverili, da se prvi in zadnji točki ujemata, pri tem morajo biti vse točke v poligonu različne (razen prve in zadnje). Hkrati bomo preverjali, da se črte ob izrisu poligona ne prekivajo ali sekaijo. Ob pojavitvi napake bomo o tem obvestili uporabnika.
```
    polygon[(1,1),(3,3),(3,1),(1,3),(1,1)];
```

== Dodatni elementi
Na zemljevidu lahko definiramo tudi dodatne elemente, ki jih lahko uporabljamo za definicijo mesta.

=== Lake
"Lake" je blok, ki ga uporabljamo za definiranje jezer. Vsebuje lahko ukaze za izris jezer.
```
    lake["Jezero Bled"]{
        COMMANDS
    };
```
=== Park
"Park" je blok, ki ga uporabljamo za definiranje parkov. Vsebuje lahko ukaze za izris parkov.
```
    park["IME"]{
        COMMANDS
    };
```

== Spremenljivke
V jeziku lahko definiramo spremenljivke, ki jih lahko uporabljamo za shranjevanje vrednosti. Spremenljivke se definirajo z uporabo ključe "let", in predstavlja točko, ki jo definiramo v oklepajih. Pred samim imenom spremenvljicke mora biti znak `$`.
```
  let @IME = (KOORDINATA X,KOORDINATA Y);
```
== Izjave
Podprte izjave v jeziku vključujejo: seštevanje, odštevanje, dostop do prve koordinate in dostop do druge koordinate.
=== Seštevanje
Seštevanje dveh točk se izvede tako, da se seštejeta obe komponenti točk. Rezultat je nova točka.
```
  polyline[(3,4),(2,1)+(3,4)];
```
=== Odštevanje
Odštevanje dveh točk se izvede tako, da se odštejeta obe komponenti točk. Rezultat je nova točka.
```
  polyline[(1,1),$q,(2,4)-$q];
```
=== First & Second
Dostop do prve in druge komponente točke se izvede tako, da se uporabita funkciji `fst` in `snd`.
```
  let @r = (fst($p),snd($q));
```
== Povpraševanja
Povpraševanje je določeno z množico točk, zapisanih v oglatih oklepajih. Poleg te množice je podana dodatna točka z določenim radijem, ki opredeljuje krožno območje povpraševanja. Rezultat povpraševanja je množica vseh točk iz začetne množice, ki ležijo znotraj tega krožnega območja.
```
  ?{[TOČKA1,TOČKA2,TOČKA3,TOČKA4],[TOČKA0,RADIJ]};
```

= Gramatika z BNF notacijo
```
  izraz   ::= SPREMENLJIVKA_DEF izraz* | POVPRAŠEVANJE izraz* | CITY izraz*
  izraz*  ::= SPREMENLJIVKA_DEF izraz* | POVPRAŠEVANJE izraz* | CITY izraz* | ε

  // definicija mesta
  CITY ::= city["IME"] { BLOKS }

  // definicija bloka
  BLOKS ::= ROAD BLOKS* | BUILDING BLOKS* | AREA BLOKS* | LAKE BLOKS* | PARK BLOKS*
  BLOKS* ::= ROAD BLOKS* | BUILDING BLOKS* | AREA BLOKS* | LAKE BLOKS* | PARK BLOKS* | ε

  ROAD ::= road["IME"] { POLYLINE }
  BUILDING ::= building["IME"] { IZRIS }
  AREA ::= area["IME"] { IZRIS }
  LAKE ::= lake["IME"] { IZRIS }
  PARK ::= park["IME"] { IZRIS }

  IZRIS ::= POLYGON | KROG

  // definicija spremenljivke
  SPREMENLJIVKA_DEF ::= let @ IME = TOČKA;

  POLYLINE ::= polyline[TOČKE];

  POLYGON ::= polygon[TOČKE];

  KROG ::= circle KROG*
  KROG* ::= [TOČKA, KROG**]
  KROG** ::= FIRST_SECOND | ŠTEVILO

  // definicija povpraševanja
  POVPRAŠEVANJE ::= ?{[TOČKE],KROG*};

  //definicija večih točk
  TOČKE ::= TOČKA TOČKE*
  TOČKE* ::= , TOČKA TOČKE* | ε

  // definicija točke
  TOČKA ::= ( KOORDINATA* , KOORDINATA* ) OPERACIJA | SPREMENLJIVKA OPERACIJA
  KOORDINATA* ::= ŠTEVILO | FIRST_SECOND

  OPERACIJA ::= + TOČKA | - TOČKA | ε

  // definicija FIRST in SECOND
  FIRST_SECOND ::= fst TOČKA | snd TOČKA

  // uporaba spremenljivke
  SPREMENLJIVKA ::= $IME

  ŠTEVILO ::= [0-9] ŠTEVILO*
  ŠTEVILO** ::= [0-9] ŠTEVILO* | . REALNO | ε
  
  REALNO ::= [0-9] REALNO*
  REALNO* ::= [0-9] REALNO* | ε

  // definicija niza
  IME ::= [a-zA-Z_] IME*
  IME* ::= [a-zA-Z0-9_] IME* | ε
```

= Izračun FIRST in FOLLOW množic !

== Izračun FIRST množic !

== Izračun FOLLOW množic !

/*= Implementacija abstraktnega sintaktičnega drevesa

= Konstrukcija končnega avtomata

= Implementacija pregledovalnika (scanner)

= Implementacija razčlenjevalnika (parser)

= Implementacija izvoza v GeoJSON

= Priprava smiselnih testnih primerov
```
city{
    road["Ptujska cesta"]{
        polyline[(1,2),(3,4),(5,6)];
    };
    area["FERI"]{
        polygon[(1,1),(1,2),(2,2),(2,1),(1,1)];
    };
    area["Park"]{
        circle[(4,3),2];
    };
    let $p = (1,1);
    let $q = (2,2);
    
    road["Ljubljanska cesta"]{
        polyline[$p,$q,$p+$q];
    };

    ?{[(1,1),$p,$q,(3,4)],[(1,1),3]};

    let $r = (fst(p),snd(q));
}
```*/
