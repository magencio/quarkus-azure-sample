# Setup services

This document explains how to setup the different services required to run this
solution with the different quarkus profiles: `%test`, `%dev` & `%prod`.

## External identity management and authentication

This example uses [Azure Active Directory (AAD) B2B](https://docs.microsoft.com/en-us/azure/active-directory/external-identities/what-is-b2b).

1. Go to the [Azure Portal](https://portal.azure.com/).

1. Go to `Azure Active Directory`.

1. In `Overview`, take note of your `Tenant ID`.

1. Go to `App registrations` in AAD. You will add 3 new app registrations.

1. Create a new registration for the API:
   1. Select a name e.g., `azure.quarkus.sample.api`.
   1. Select Supported account types (e.g., `Accounts in this organizational directory only`).
   1. Add a Web Redirect Uri `https://oauth.pstmn.io/v1/callback` to be able to authenticate users with Postman.
   1. Register the app.
   1. In `Overview`, take note of the `Application (client) ID`.
   1. In `Authentication`, enable `Implicit grant and hybrid flows`.
   1. In `Certificate & secrets`, add a new `client secret` and take note of it.
   1. In `Expose an API`, set the `Application ID URI` to the default one and add a scope called `API.Access`
      that both Admins and Users can consent
      (this scope will be used by the Postman collection to authenticate users).
   1. In `App roles` create two app roles: `Producer` and `Consumer` that allow both Users/Groups and Applications
      as member types.

1. Create a new registration for a Producer app:
   1. Select a name e.g., `azure.quarkus.sample.producer`.
   1. Register the app.
   1. In `Overview`, take note of the `Application (client) ID`.
   1. In `Certificate & secrets`, add a new `client secret` and take note of it.
   1. In `API permissions`, add a permission, select `azure.quarkus.sample.api` in `My APIs`,
      then `Application permissions`, and select `Producer` permission. This is the way to assign a
      Producer role to an app.
      >**_NOTE:_** This operation might require an AAD Admin to `Grant admin consent` for the requested permissions.
       Without this consent you can authenticate with this app, but its token won't contain the Producer role.

1. Create a new registration for a Consumer app:
   1. Select a name e.g., `azure.quarkus.sample.consumer`.
   1. Register the app.
   1. In `Overview`, take note of the `Application (client) ID`.
   1. In `Certificate & secrets`, add a new `client secret` and take note of it.
   1. In `API permissions`, add a permission, select `azure.quarkus.sample.api` in `My APIs`,
      then `Application permissions`, and select `Consumer` permission. This is the way to assign a
      Consumer role to an app.
      >**_NOTE:_** This operation might require an AAD Admin to `Grant admin consent` for the requested permissions.
       Without this consent you can authenticate with this app, but its token won't contain the Consumer role.

1. To assign roles to users, go to `Enterprise applications` in AAD.
   1. In `All applications`, select `azure.quarkus.sample.api`.
   1. In `Users and groups`, add a User/Group and select a role (either Producer or Consumer).
      >**_NOTE:_** You should also see under `Users and groups` the roles assigned to `azure.quarkus.sample.producer`
       and `azure.quarkus.sample.consumer`.

### Configure authentication for the APIs: %test and %dev profiles

Go to [application.properties for producer-api](../src/producer-api/src/main/resources/application.properties)
and [application.properties for consumer-api](../src/consumer-api/src/main/resources/application.properties)
and update the following properties:

```properties
%dev.quarkus.oidc.auth-server-url
%dev.quarkus.oidc.client-id
```

See comments in `application.properties` for more details.

### Configure authentication for the APIs: %prod profile

You must set the following environment variables used by
[application.properties for producer-api](../src/producer-api/src/main/resources/application.properties)
and [application.properties for consumer-api](../src/consumer-api/src/main/resources/application.properties):

```properties
OIDC_AUTH_SERVER_URL
OIDC_CLIENT_ID
```

See comments in `application.properties` for more details.

### Configure authentication for Postman

You must set the following variables in your
[Postman environment](../postman/localhost.postman_environment.json):

```properties
tenant
api-client-id
api-client-secret
producer-client-id
producer-client-secret
consumer-client-id
consumer-client-secret
```

## Asynchronous communication between APIs

This example uses [Azure Event Hubs for Kafka](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-for-kafka-ecosystem-overview).

>**_NOTE:_** Kafka surface for Event Hubs is available from the Standard pricing tier.

1. Go to the [Azure Portal](https://portal.azure.com/).

1. Go to your `Event Hubs Namespace`.

1. Take note of the name of your namespace.

1. In `Share access policies`, select the default policy (or create a new one), and take note
   of its `Connection string`.

### Configure Kafka for the APIs: %test and %dev profiles

By default, the APIs are using the Dev services for Kafka, so you don't need to configure anything else.

You can connect [Offset Explorer](https://kafkatool.com/features.html) to the Dev services:

1. `Add New Connection...`.
1. In `Properties`, select a `Cluster name`, select `Kafka Cluster Version` (e.g., 2.8),
   and leave `Zookeeper Host` and `Zookeeper Port` empty.
1. In `Advanced`, set `Boostrap servers` to `localhost:49884`
   (use the same port that has been fixed in `application.properties`).
1. Leave the rest as is.

### Configure Kafka for the APIs: %prod profile

You must set the following environment variables used by
[application.properties for producer-api](../src/producer-api/src/main/resources/application.properties)
and [application.properties for consumer-api](../src/consumer-api/src/main/resources/application.properties):

```properties
KAFKA_BOOTSTRAP_SERVER  
KAFKA_CONNECTION_STRING
```

See comments in `application.properties` for more details.

You can connect [Offset Explorer](https://kafkatool.com/features.html) to Event Hubs:

1. `Add New Connection...`.
1. In `Properties`, select a `Cluster name`, select `Kafka Cluster Version` (e.g., 2.8),
   and leave `Zookeeper Host` and `Zookeeper Port` empty.
1. In `Security`, set `Type` to `SASL SSL`.
1. In `Advanced`, set `Boostrap servers` to the same value as KAFKA_BOOTSTRAP_SERVER
   environment variable, and `SASL Mechanism` to `PLAIN`.
1. In `JAAS Config`,  set `Value` to
   `org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="${KAFKA_CONNECTION_STRING}";`, but replace `${KAFKA_CONNECTION_STRING}`
   with the value of KAFKA_BOOTSTRAP_SERVER environment variable.
1. Leave the rest as is.

## NoSQL database

This example uses [Azure CosmosDB with MongoDB API](https://docs.microsoft.com/en-us/azure/cosmos-db/mongodb/mongodb-introduction).

1. Go to the [Azure Portal](https://portal.azure.com/).

1. Go to your `Azure Cosmos DB API for MongoDB account`.

1. In `Connection String`, take note of the MongoDB connection string.

>**_NOTE:_** You don't need to create any database or collection in your Azure CosmosDB. They will be created
 by the code the first time they are needed.

### Configure MongoDB for the APIs: %test and %dev profiles

By default, the APIs are using the Dev services for MongoDB, so you don't need to configure anything else.

You can connect [MongoDB Compass](https://docs.mongodb.com/compass/master/install/) to the Dev Services
using `mongodb://localhost:49885` as the connection string
(use the same port that has been fixed in `application.properties`).

### Configure MongoDB for the APIs: %prod profile

You must set the following environment variable used by
[application.properties for consumer-api](../src/consumer-api/src/main/resources/application.properties):

```properties
MONGODB_CONNECTION_STRING
```

See comments in `application.properties` for more details.

You can connect [MongoDB Compass](https://docs.mongodb.com/compass/master/install/) to Azure Cosmos DB using the same
connection string as in MONGODB_CONNECTION_STRING environment variable. You may also inspect the contents of the
database via Cosmos DB's `Data Explorer` in the Azure Portal.

## Application Performance Management (APM) and monitoring

This example uses [Azure Application Insights](https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview).

1. Go to the [Azure Portal](https://portal.azure.com/).

1. Go to your `Application Insights`.

1. In `Overview`, take note of the `Connection String`.

### Configure Application Insights for the APIs: all profiles

You must set the following environment variable used by ApplicationInsights Java Agent:

```properties
APPLICATIONINSIGHTS_CONNECTION_STRING
```

You can go to your Application Insights in the Azure Portal to see what system is sending to the service.
