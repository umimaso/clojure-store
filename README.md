# Clojure Store
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

# Resources used
- [Luminus Documentation](https://luminusweb.com/)
- [Clojure Cheatsheet](https://clojure.org/api/cheatsheet)
- [Clojure by Example](https://kimh.github.io/clojure-by-example/)
- [Selmer](https://github.com/yogthos/Selmer)
- [Bulma](https://bulma.io/documentation/)
- [Font Awesome Icons](https://fontawesome.com/icons/)
- [HugSQL, used in conman](https://www.hugsql.org/)
- [Struct](https://funcool.github.io/struct/latest/)

