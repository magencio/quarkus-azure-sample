# consumer-api

Folder [src/consumer-api](.) contains the API that both Producers and Consumers can use to retrieve the results
of the operations.

`consumer-api` will get messages with operations from a Kafka topic. It will validate the messages, calculate
the result of the operations, and store those results in a MongoDB database.
It also provides an endpoint for the Producers and Consumers to retrieve those results from the database.

Producers can only get the results of the operations they sent, while Consumers can get the results of any operation
sent by any Producer.

## Development details

Check the [pre-requisites](../../README.md#pre-requisites) before continuing.

You can use provided [Postman collection](../../postman/README.md) to test the API.

Run the following commands in folder [src/consumer-api](.).

## Running the application in dev mode

1. Install the application. This will download all dependencies, build it, run all unit and integration tests,
   and generate the required jar file for the `com.microsoft.azure.applicationinsights-agent` in
   `target/quarkus-app/lib/main` folder:

   ```bash
   mvn clean install
   ```

2. Then run the application in dev mode (which also enables live coding):

   ```bash
   mvn compile quarkus:dev
   ```

> **_NOTE:_** Quarkus ships with a Dev UI, which is available in dev mode only at
  [http://localhost:8081/q/dev/](http://localhost:8081/q/dev/).

## Debugging the application with Visual Studio Code

[Quarkus extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-quarkus) provides a
`Quarkus: Debug current Quarkus project` that launches the Maven quarkus:dev plugin and
automatically attaches a debugger.

## Running the tests of the application

- To run the unit tests:

  ```bash
  mvn clean test
  ```

- To run both the unit and the integration tests:

  ```bash
  mvn clean verify
  ```

> **_NOTE:_** Visual Studio Code will allow you to run and debug unit and integration tests within the editor.

## Packaging and running the application

- The application can be packaged using:

  ```bash
  mvn clean package
  ```

  It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
  Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

  The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

- If you want to build an _über-jar_, execute the following command:

  ```bash
  mvn clean package -Dquarkus.package.type=uber-jar
  ```

  The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

>**_NOTE:_** All required environment variables for the `%prod` profile must be set for the application to work.
 See [How to setup services](../../docs/setup-services.md) for details.

## Creating a native executable

- You can create a native executable using:

  ```bash
  mvn package -Pnative
  ```

  Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

  ```bash
  mvn package -Pnative -Dquarkus.native.container-build=true
  ```

  You can then execute your native executable with: `./target/consumer-api-1.0.0-SNAPSHOT-runner`

- If you want to learn more about building native executables, please consult
  [Building Applications with Maven](https://quarkus.io/guides/maven-tooling).

>**_NOTE:_** All required environment variables for the `%prod` profile must be set for the application to work.
 See [How to setup services](../../docs/setup-services.md) for details.

### Creating a Docker image with the application and running it

1. Package the application:

   ```bash
   mvn clean package
   ```

1. Create a Docker image for this application:

   ```bash
   docker build -f src/main/docker/Dockerfile.jvm -t consumer-api:dev .
   ```

1. Start a container running this application:

   ```bash
   docker run -it --rm \
     -e APPLICATIONINSIGHTS_CONNECTION_STRING \
     -e OIDC_AUTH_SERVER_URL \
     -e OIDC_CLIENT_ID \
     -e KAFKA_BOOTSTRAP_SERVER \
     -e KAFKA_CONNECTION_STRING \
     -e MONGODB_CONNECTION_STRING \
     -p 8081:8080 consumer-api:dev
   ```

   You can now target the application at localhost:8081.

>**_NOTE:_** All required environment variables for the `%prod` profile must be set for the application to work.
 See [How to setup services](../../docs/setup-services.md) for details.
