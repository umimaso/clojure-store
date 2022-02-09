# Clojure Store
## Formatting
- Check formatting with `lein cljfmt check`
- Fix formatting with `lein cljfmt fix`

## Migrations
To create a new migration
- lein repl
- user=> (start)
- user=> (user/create-migration "migration name")

To run all migrations
- lein run migrate

To rollback the latest migration
- lein run rollback

# References
- [Luminus Documentation](https://luminusweb.com/)
- [Clojure Cheatsheet](https://clojure.org/api/cheatsheet)
