# Source code

Folder [src](.) contains all the source code of the example:

- [quarkus-parent](quarkus-parent/README.md): the parent POM and other files that are common to the APIs.
- [producer-api](producer-api/README.md): the API that Producers can use to send operations to the system.
- [consumer-api](consumer-api/README.md): the API that both Producers and Consumers can use to retrieve the
  results of the operations.

## How to install all applications

Run the following command in [src](.) folder to download all dependencies, build all applications,
run their unit and integration tests, and generate the required jar file for the
`com.microsoft.azure.applicationinsights-agent` in their respective `target/quarkus-app/lib/main` folder:

```bash
mvn clean install
```
