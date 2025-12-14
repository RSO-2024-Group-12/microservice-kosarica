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

## Zagon

Zagon v dev načinu.

```shell script
./mvnw quarkus:dev
```