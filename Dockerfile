FROM openjdk:8-alpine

COPY target/uberjar/clojure-store.jar /clojure-store/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clojure-store/app.jar"]
