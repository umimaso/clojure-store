# Clojure Store
## Requirements
- [leiningen](https://leiningen.org/#install)

## Running
- Run the initial migrations with `lein run migrate` if it's the first time starting the server.
- Start the server with `lein run`

This will start a local server running on localhost at port 3000.

The swagger API documentation will also be available at http://localhost:3000/swagger-ui/index.html.

## Formatting
Uses Clojure plugin cljfmt
- Check formatting with `lein cljfmt check`
- Fix formatting with `lein cljfmt fix`

## Testing
Run Clojure tests with `lein test`

## Migrations
To create a new migration
- lein repl
- user=> (start)
- user=> (user/create-migration "migration name")

To run all migrations
- lein run migrate

To rollback the latest migration
- lein run rollback

Delete database, and apply all migrations
- lein run reset

## Resources used
- [Luminus Documentation](https://luminusweb.com/)
- [Clojure Cheatsheet](https://clojure.org/api/cheatsheet)
- [Clojure by Example](https://kimh.github.io/clojure-by-example/)
- [Selmer](https://github.com/yogthos/Selmer)
- [Bulma](https://bulma.io/documentation/)
- [Font Awesome Icons](https://fontawesome.com/icons/)
- [HugSQL, used in conman](https://www.hugsql.org/)
- [Struct](https://funcool.github.io/struct/latest/)

## License
[LGPLv3](https://github.com/umimaso/clojure-store/blob/main/LICENSE.txt)
