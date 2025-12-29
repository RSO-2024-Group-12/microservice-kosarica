# Košarica (microservice-kosarica)

Mikrostoritev omogoča upravljanje in pregled nakupovalnih košaric uporabnikov.

## Namen

- pridobitev nakupovalne košarice uporabnika
- dodajanje novih izdelkov v nakupovalno košarico
- posodabljanje količine izdelkov v nakupovani košarici
- odstranitev vseh izdelkov iz košarice
- 
## Tehnologije

- Java 21
- Quarkus
- PostgreSQL
- Hibernate ORM
- REST
- gRPC
- OpenAPI
- Swagger

## Integracije

### Odvisnosti

Spodaj so navedene mikrostoritve, ki jih microservice-kosarica uporablja za svoje delovanje.

| Mikrostoritev          | Komunikacija | Namen                                         |
|------------------------|--------------|-----------------------------------------------|
| microservice-izdelki   | REST (GET)   | pridobitev podatkov o izdelku                 |
| microservice-skladisce | REST (GET)   | preverjanje zaloge pred dodajanjem v košarico |

## API

### REST
- `GET v1/kosarica/{id}` - pridobitev nakupovalne košarice uporabnika
- `POST v1/kosarica` - dodajanje novega izdelka v košarico
- `PUT v1/kosarica` - posodabljanje količine izdelka v košarici
- `DELETE v1/kosarica/{id}` - odstranitev vseh izdelkov iz košarice

Podrobna dokumetacija je na voljo preko **OpenAPI (Swagger UI)**.

### gRPC
Service `gRPCKosaricaService`:
- `getKosarica` - pridobitev nakupovalne košarice uporabnika
- `createKosarica` - dodajanje novega izdelka v košarico
- `updateKosarica` - posodabljanje količine izdelka v košarici
- `deleteKosarica` - odstranitev vseh izdelkov iz košarice

Podrobnosti so definirane v datoteki **proto**.

## Razvoj in zagon

### Lokalni zagon v razvojnem načinu

Za zagon aplikacije s podporo za "vroče" ponovno nalaganje kode (live coding) uporabite:

```shell script
./mvnw quarkus:dev
```

Aplikacija bo privzeto dostopna na `http://localhost:8081`. Razvojni vmesnik (Dev UI) je na voljo na `http://localhost:8081/q/dev/`.

### Pakiranje aplikacije

Za pakiranje aplikacije v JAR datoteko:

```shell script
./mvnw package
```

Za izdelavo *über-jar* (vsebuje vse odvisnosti):

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

### Izgradnja Docker slike

Aplikacijo lahko zapakirate v Docker sliko z ukazom:

```shell script
docker build -t nakupify/microservice-kosarica .
```

## Konfiguracija

Konfiguracijski parametri se nahajajo v `src/main/resources/application.properties`. Glavne nastavitve vključujejo:

- `quarkus.datasource.jdbc.url`: Povezava do PostgreSQL baze.

## Avtomatski testi

Za zagon vseh testov uporabite:

```shell script
./mvnw test
```