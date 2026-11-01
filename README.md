# JavaFX Temperature Converter

Java 21 / Maven temperature converter with FXML, five UTF-8 ResourceBundles, JUnit, JaCoCo, Jenkins, Docker and Kubernetes configuration.

## Run locally

Install JDK 21 and Maven 3.9+, then run from this directory:

```sh
mvn clean verify
mvn javafx:run
```

Choose English, Finnish, Swedish, Japanese or Arabic, select the conversion direction, enter a temperature, and click Convert. Arabic uses right-to-left layout. Decimal points, decimal commas and Arabic digits/separators are supported; grouping separators are not supported. Non-finite inputs and conversion overflow are rejected. Values below -40 Celsius or above 50 Celsius trigger a warning.

Language switching preserves input and conversion direction, and clears previous results. Keep all five UTF-8 property files separate; the base bundle is English.

## Code

- LocalizationApp.java: JavaFX startup.
- MainController.java: conversion actions, language switching and number formatting.
- TemperatureConverter.java: both conversions and extreme-temperature detection.
- NumberParser.java: input normalization and validation.
- src/main/resources/org/example/main.fxml: interface with ResourceBundle references.
- MessagesBundle*.properties: five languages.

## Tests

The verify command runs tests and enforces at least 90% line coverage individually for TemperatureConverter and NumberParser. Open target/site/jacoco/index.html for coverage.

On a machine with a graphical display, also run:

```sh
mvn -DguiTests=true clean verify
```

This runs the optional FXML/controller smoke test, checking languages, Arabic orientation and conversion. Visual rendering and X-server display still require manual checks.

## Docker and Windows X server

Start Docker Desktop in Linux-container mode. Start XMing or VcXsrv on display 0 with TCP enabled. Authorize the Docker/WSL source address in the X server and permit its connection to TCP port 6000 through your private-network firewall. Avoid globally disabling access control.

```sh
docker build -t YOUR_DOCKERHUB_USERNAME/temperature-converter:1.0 .
docker run --rm --name temperature-converter -e DISPLAY=host.docker.internal:0.0 YOUR_DOCKERHUB_USERNAME/temperature-converter:1.0
```

The image builds Linux JavaFX dependencies and includes Japanese and Arabic fonts. If no window appears, inspect docker logs temperature-converter, the DISPLAY address, X-server access and firewall settings. Cookie-authenticated X servers also require a read-only mount of an authorized Xauthority file and XAUTHORITY set to its container path, readable by the app user.

Publish after verifying the image:

```sh
docker login
docker push YOUR_DOCKERHUB_USERNAME/temperature-converter:1.0
```

## Jenkins

1. Push this project to your GitHub repository.
2. Configure a trusted Linux Jenkins agent labelled docker with Docker daemon access.
3. Install Pipeline, Git, Docker Pipeline, Credentials Binding and JUnit plugins.
4. Add a username/password credential with ID dockerhub-credentials: your Docker Hub username and access token.
5. Create a Pipeline from SCM job pointing to your repository and Jenkinsfile on your trusted submission branch.
6. Set DOCKERHUB_REPOSITORY to yourusername/temperature-converter and run it.

After initial configuration, the pipeline polls SCM every five minutes. It runs tests and coverage checks, archives reports, builds the image and pushes the Jenkins build-number and latest tags. Credentials are not stored in the project.

## Kubernetes

Publish your image, then edit k8s/deployment.yaml with your actual image/tag and a DISPLAY host reachable from the pods. The supplied address is a Docker Desktop starting point; other clusters may need a different address and X-server authorization.

```sh
kubectl apply -f k8s/deployment.yaml
kubectl rollout status deployment/temperature-converter --timeout=120s
kubectl get pods
kubectl logs deployment/temperature-converter
```

Verify the window appears through the X server and check conversions and all languages. A running Pod alone does not verify the GUI. No Service or persistent storage is needed. Cleanup:

```sh
kubectl delete deployment temperature-converter
```

## Submission

Submit your actual GitHub link, Docker Hub image URL and real screenshots of the containerized GUI running through your X server. Check both conversion directions, invalid numbers, all five languages, Japanese/Arabic glyphs and extreme-temperature boundaries. External deployment and screenshots must be completed in your environment.
