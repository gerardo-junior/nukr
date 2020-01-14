# nukr

Nukr - The new business social media

## Come on, do your tests

#### But what will you need?

- [docker](https://docs.docker.com/install/) ~ 18.04.0-ce
- [docker-compose](https://docs.docker.com/compose/) ~ 1.21.1

#### Okay, how to put it to up?

First clone of the project
```bash
docker-compose up
```
*~ Tip: using the docker-compose you can add `-d` and you can keep with your terminal ~*

Wait until the build is finished and the database service has finished creating the database.

#### After image build

now you can execute any program inside the server with command like it.
```bash
docker-compose exec api lein eastword
```

## Create the PostgreSQL database for development e test

Migrations are managed by [migratus](https://github.com/yogthos/migratus) to begin working initially
run both:

```sh
$ docker-compose exec api lein migratus migrate
$ docker-compose exec api lein with-profile test migratus migrate
```

### Running Server

```sh
$ docker-compose exec api lein run -m nukr.server 3000
```

Now you can take a look at [http://0.0.0.0:3000/spec](http://0.0.0.0:3000/spec)

#### Example create a new user

```sh
$ curl -X POST \
  http://localhost:3000/api/v1/user \
  -H 'Content-Type: application/json' \
  -d '{ 
  "email":"emaisl@test.com",
  "username":"usernasdasdame12",
  "password":"123456789"	
}'
```

#### Example get access token

```sh
$ curl -X GET http://usernasdasdame12:123456789@localhost:3000/api/v1/auth
```

Tip: if you will use 'curl' put the token in a environment variable, if you have jq installed follow the command
```sh
ACCESS_TOKEN=$(curl -X GET -s "http://usernasdasdame12:123456789@localhost:3000/api/v1/auth" | jq .token | tr -d '"')
```
#### Example route with token

```sh
$ curl -X GET \
  http://localhost:3000/api/v1/user/connection-suggestions \
  -H "Authorization: Token $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{  
   "page": 1,
   "per-page": 10
}'
```

### Running Tests

```sh
$ docker-compose exec api lein eastword
$ docker-compose exec api lein test
```

*NOTE* Test will fail the first run after migrations due to a duplicate key.

### Documentation

The HTML documentation can generated locally with `docker-compose exec api lein doc` the output will be
saved in `doc/api`.


## Summarizing an installation

```sh
$ docker-compose up -d # raises the database, and clojure server with lein repl active on port: 12345
$ docker-compose exec api lein migratus migrate
$ docker-compose exec api lein with-profile test migratus migrate
$ docker-compose exec api lein run -m nukr.server 3000
```