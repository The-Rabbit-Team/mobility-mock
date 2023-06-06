# mobility-mock

[![Discord](https://img.shields.io/discord/1095829734211977276?label=Discord&style=flat-square)](https://discord.gg/9u69mxsFT6)

<p align="center">
  <img src="https://github.com/The-Rabbit-Team/.github/blob/master/banners/mobility-mock.png?raw=true" />
</p>

Ce projet est un mock de l'API Skolengo. Il s'agit d'une fausse API, en lecture-seule, permettant de tester l'implémentation du typage.

## Variables d'environnement
* `MOCK_PORT`: Le port du serveur HTTP à utiliser. Par défaut, le port utilisé est 3000.
* `MOCK_DB_URL`: L'URL d'une instance de RethinkDB au format `rethinkdb://rethinkdb/database`.

## Exemple de `docker-compose.yml`
Le fichier **[docker-compose.yml](./docker-compose.yml)** est un exemple de liaison entre un serveur RethinkDB et ce serveur mock API.
