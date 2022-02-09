# Clojure Store
## Formatting
Uses Clojure plugin cljfmt
- Check formatting with `lein cljfmt check`
- Fix formatting with `lein cljfmt fix`

## Linting
### SQL
Uses Python package sqlfluff (`pip install sqlfluff`)
- Check for SQL errors with `sqlfluff lint`
- Fix errors with `sqlfluff fix`

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

# References
- [Luminus Documentation](https://luminusweb.com/)
- [Clojure Cheatsheet](https://clojure.org/api/cheatsheet)
