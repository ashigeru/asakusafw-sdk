# Asakusa SDK

Asakusa Software Development Kit (Asakusa SDK) helps developing batch applications using [Asakusa Framework](https://github.com/asakusafw/asakusafw).

This project includes following features:

* Asakusa Gradle Plug-ins
  * Asakusa Batch Application Plugin
  * Asakusa Framework Organizer Plugin
* Maven Archetypes (deprecated)
  * Direct I/O project
  * WindGate project
  * ThunderGate project
  * Framework Organizer project 

## How to build

### Gradle plug-ins

```sh
cd gradle-sdk-project
./gradlew clean [build] install
```

### Maven archetypes

* requires Maven `>= 3.2.3`

```sh
cd maven-sdk-project
mvn clean install
```

## License
* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
