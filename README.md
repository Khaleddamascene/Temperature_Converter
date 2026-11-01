# Temperature Converter

A JavaFX application for converting temperatures between Celsius and Fahrenheit, with English, Finnish, Swedish, Japanese and Arabic interfaces.

## Links

- [GitHub repository](https://github.com/Khaleddamascene/Temperature_Converter)
- [Docker Hub image](https://hub.docker.com/r/khaledmarai/temperature_converter)
- Image name: `khaledmarai/temperature_converter:latest`

## Features

- Fahrenheit to Celsius and Celsius to Fahrenheit conversion.
- Dynamic language switching using FXML and UTF-8 ResourceBundles.
- Right-to-left layout for Arabic.
- Decimal point, decimal comma and Arabic numeric input support.
- Invalid-number validation and extreme-temperature warnings.
- JUnit tests, JaCoCo coverage, Jenkins pipeline and Docker configuration.

## Run locally

Install JDK 21 and Maven 3.9 or later.

```powershell
git clone https://github.com/Khaleddamascene/Temperature_Converter.git
cd Temperature_Converter
mvn clean verify
mvn javafx:run
```

## Run with Docker on Windows

Start Docker Desktop in Linux-container mode. Start XMing or VcXsrv on display 0 with TCP enabled. Authorize the Docker/WSL source address in the X server and permit its connection through the Windows firewall on TCP port 6000.

Pull and run the image:

```powershell
docker pull khaledmarai/temperature_converter:latest
docker run --rm --name temperature-converter -e DISPLAY=host.docker.internal:0.0 khaledmarai/temperature_converter:latest
```

Run each command on one line in PowerShell. Keep the X server running while using the application.

### Why DISPLAY is required

The application runs inside a Linux container, while XMing or VcXsrv displays its window on Windows.

- `-e` sets an environment variable inside the container.
- `DISPLAY` specifies the graphical display address.
- `host.docker.internal` points to the Windows host from Docker Desktop.
- `:0.0` selects X display 0, screen 0.

## Build and publish

Run from the project directory containing Dockerfile, pom.xml and src:

```powershell
docker build -t khaledmarai/temperature_converter:latest .
```

The multistage Dockerfile compiles the application and downloads Linux JavaFX dependencies inside its Maven build stage.

To publish, authenticate with an account authorized to push to this repository. Use a Docker Hub access token when prompted for a password:

```powershell
docker login -u khaledmarai
docker push khaledmarai/temperature_converter:latest
```

Rebuild after changing source code, FXML, translations or the Dockerfile, then start a new container. Push the updated image if other machines need it.

## Tests and coverage

```powershell
mvn clean verify
```

Open `target/site/jacoco/index.html` to inspect coverage.

To include the optional GUI tests on a machine with a working graphical display:

```powershell
mvn -DguiTests=true clean verify
```

GUI tests are disabled by default. Therefore, MainController may show 0% coverage in a standard run. A Jenkins service may require additional display setup to execute GUI tests.

## Jenkins configuration

The Windows pipeline requires:

- JDK 21 and a Maven tool installation named `Maven3`.
- A Windows agent for stages using `bat`.
- Docker CLI and Docker daemon access for the account running the agent.
- Docker Pipeline and JUnit plugins.
- Username/password credential ID `Temperature_Converter_ID`, with the Docker Hub username and access token.
- `DOCKERHUB_REPO = 'khaledmarai/temperature_converter'`.
- `DOCKER_IMAGE_TAG = 'latest'`.

Use `mvn -B clean verify` to build, test and generate/check coverage. Publish JUnit results from `target/surefire-reports/*.xml` and archive `target/site/jacoco/**`. A pipeline using `jacoco()` also requires a compatible coverage publishing plugin.

Commit changes to GitHub before triggering Jenkins so its checkout receives the current source and Dockerfile. Build and push stages publish the image using the configured credentials.

The original bundled Jenkinsfile is a separate Linux-agent variant using the `docker` agent label, `dockerhub-credentials` credential and `DOCKERHUB_REPOSITORY` parameter. If using that variant, configure those names and set the parameter to `khaledmarai/temperature_converter`.

## Troubleshooting

### Unable to open DISPLAY

Use the complete run command with `-e DISPLAY=host.docker.internal:0.0`. Ensure the X server is running and permits connections from Docker/WSL.

```powershell
Test-NetConnection localhost -Port 6000
```

A successful result confirms Windows can reach that port; it does not prove Docker is authorized. Check X-server listening settings, access controls and firewall rules if the container still cannot connect.

### Japanese text appears as squares

Install `fontconfig`, `fonts-noto-core` and `fonts-noto-cjk` in the runtime image and run `fc-cache -f` after installation.

Check Japanese fonts inside the image:

```powershell
docker run --rm --entrypoint fc-list khaledmarai/temperature_converter:latest :lang=ja family
```

If Noto Sans CJK JP is installed but not selected, add its family to the root VBox style in main.fxml:

```xml
style="-fx-font-size: 15px; -fx-font-family: 'Noto Sans CJK JP';"
```

Rebuild and restart, then recheck all five languages:

```powershell
docker build --no-cache -t khaledmarai/temperature_converter:latest .
docker run --rm -e DISPLAY=host.docker.internal:0.0 khaledmarai/temperature_converter:latest
```

## Kubernetes test

The deployment in `k8s/deployment.yaml` runs one instance of `khaledmarai/temperature_converter:latest`. Its `DISPLAY` defaults to `host.minikube.internal:0.0` (display 0, TCP port 6000). This is minikube's host-access name; its address depends on the driver. See the [minikube host-access documentation](https://minikube.sigs.k8s.io/docs/handbook/host-access/).

For a new Docker-based minikube profile, start Docker Desktop in Linux-container mode, then start the cluster:

```powershell
minikube start --driver=docker
```

If your existing `minikube` profile uses Hyper-V, use `minikube start` to keep its configured driver instead. Run it from a PowerShell session with permission to manage Hyper-V. Do not change an existing profile's driver using the command above.

Start XMing or VcXsrv on Windows with display 0 and TCP listening enabled. It must listen on an interface reachable from minikube, not only localhost. Allow the cluster's source address through the X server access controls and Windows firewall on TCP port 6000.

Check connectivity from the minikube node before deploying:

```powershell
minikube ssh -- "nc -vz host.minikube.internal 6000"
```

If that address does not reach the Windows X server, run `ipconfig` and use a Windows IPv4 address reachable from minikube. Test it with `minikube ssh -- "nc -vz <WINDOWS-IP> 6000"`, replacing `<WINDOWS-IP>` with the actual address. Set the YAML's `DISPLAY` value to that address followed by `:0.0`. Do not use `localhost` or the output of `minikube ip`: those refer to the pod or cluster node, not the Windows X server.

Deploy explicitly to the minikube context:

```powershell
kubectl --context=minikube apply -f k8s/deployment.yaml
kubectl --context=minikube rollout status deployment/temperature-converter --timeout=120s
kubectl --context=minikube get pods -l app=temperature-converter
kubectl --context=minikube logs deployment/temperature-converter
```

Verify the GUI appears through the X server and test conversion and languages. A running Pod alone does not establish that the GUI works.

If the logs report `Unable to open DISPLAY`, check the address, TCP connectivity, and X server authorization. Node connectivity does not guarantee that the hostname resolves inside the pod; if necessary, use the tested Windows IPv4 address in the YAML and apply it again. No Kubernetes Service or port forwarding is needed: the application connects outward to the X server. Closing the GUI exits the application, so Kubernetes will restart it; delete the deployment when finished.

Cleanup:

```powershell
kubectl --context=minikube delete -f k8s/deployment.yaml
```

## Submission

Submit the GitHub and Docker Hub links above and real screenshots of the containerized GUI running through XMing or VcXsrv. Include Jenkins, coverage and Kubernetes evidence as required. These instructions do not themselves certify successful deployment.
