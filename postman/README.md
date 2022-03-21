# Postman collection

Folder [postman](.) contains a Postman collection and a sample environment that can be used to test the APIs.

## Before you start

Check the [pre-requisites](../README.md#pre-requisites) before continuing.

## Test the APIs with Postman

1. Install [Postman](https://www.postman.com/downloads/).
1. Import the collection to your Collections, and the enviroment to your Environments.
1. Edit variables and add missing secrets to the enviroment you just imported.
1. Select any request in the collection and send it.

>**_NOTE:_** The collection includes a request to autenticate an application that acts
as a Producer, and a request to authenticate an application that acts as a Consumer.
Requests to [producer-api](../src/producer-api/) and [consumer-api](../src/consumer-api/)
endpoints are configured by default to use the access token retrieved by those
authentication requests, as seen in the `Authorization` section of each request.
>
> We can also authenticate with a user instead of with those applications.
The `Auth` section of the collection will allow us to `Get a new Access Token` for that
user. To use the token of the user, go to the `Authorization` section of the desired request
and change the `Type` from `Bearer Token` to `Inherit auth from parent`.

## Run end-to-end test with Postman Runner

1. Select the collection and the environment you imported and configured in
   [Test the APIs with Postman](#test-the-apis-with-postman).
1. Select the option `Run collection`.

See [Using the Collection Runner](https://learning.postman.com/docs/running-collections/intro-to-collection-runs/)
   for more details.

## Run end-to-end test with Newman

1. [Install Newman](https://support.postman.com/hc/en-us/articles/115003703325-How-to-install-Newman)
1. Edit variables and add missing secrets to `localhost.postman_environment.json`.
1. Run:

   ```bash
   newman run azure_quarkus_sample.postman_collection.json -e localhost.postman_environment.json 
   ```

See [Running collections on the command line with Newman](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/#using-newman-with-cicd)
for more details.

> **_NOTE:_** You can run `newman` in your CI/CD pipelines.
  See [Using Newman with CI/CD](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/#using-newman-with-cicd)
  and [CI with Postman API](https://learning.postman.com/docs/running-collections/using-newman-cli/continuous-integration/) for details.
