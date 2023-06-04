# mobility-mock

[![Discord](https://img.shields.io/discord/1095829734211977276?label=Discord&style=flat-square)](https://discord.gg/9u69mxsFT6)

<p align="center">
  <img src="https://github.com/The-Rabbit-Team/.github/blob/master/banners/mobility-mock.png?raw=true" />
</p>

Ce projet est un mock de l'API Skolengo. Il s'agit d'une fausse API, en lecture-seule, permettant de tester l'implémentation du typage.

## Variables d'environnement
* `MOCK_PORT`: Le port du serveur HTTP à utiliser (*défaut: 3000*)
* `MOCK_DB_URL`: L'URL d'une instance de RethinkDB (*rethinkdb://rethinkdb/database*)

## Exemple de `docker-compose.yml`
```yml
version: "3.9"

services:
  mobility-mock:
    build: .
    restart: always
    depends_on:
      - rethinkdb-server
    networks:
      - mobility-mock
    ports:
      - 3000:3000
    environment:
      MOCK_PORT: 3000
      MOCK_DB_URL: rethinkdb://rethinkdb-server/database

  rethinkdb-server:
    image: rethinkdb:latest
    restart: always
    networks:
      - mobility-mock
    volumes:
      - rethinkdb-storage:/data

networks:
  mobility-mock:

volumes:
  rethinkdb-storage:
```
