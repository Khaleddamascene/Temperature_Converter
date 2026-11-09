# Temperature Converter

A JavaFX application for converting temperatures between Celsius, Fahrenheit and Kelvin, with English, Finnish, Swedish, Japanese and Arabic interfaces.

## Links

- [GitHub repository](https://github.com/Khaleddamascene/Temperature_Converter)
- [Docker Hub image](https://hub.docker.com/r/khaledmarai/temperature_converter)
- Image name: `khaledmarai/temperature_converter:latest`

## Features

- All six conversion directions, with the last five successful results shown newest first.
- Dynamic language switching using FXML and UTF-8 ResourceBundles.
- Right-to-left layout for Arabic.
- Decimal point, decimal comma and Arabic numeric input support.
- Invalid-number validation and extreme-temperature warnings.
- JUnit tests, JaCoCo coverage, Jenkins pipeline and Docker configuration.

## Database localization

I chose the **translation table approach** for database localization. The MariaDB database, `temperature_converter`, stores six units with names and descriptions in English, Finnish, Swedish, Japanese and Arabic. The database uses `utf8mb4`, and the UTF-8 `init.sql` script creates the tables and inserts all 30 translations.

The schema has a language-neutral `unit(id)` table containing the identifiers `celsius`, `fahrenheit`, `kelvin`, `km`, `hour` and `kmh`. The related `unit_translation(unit_id, lang, name, description)` table holds the translated text. Its composite primary key, `(unit_id, lang)`, allows one translation per unit and language. The foreign key `unit_id` references `unit.id`. The `conversion` table stores each input, source unit, target unit, result, language and timestamp. Its source and target columns reference `unit.id`. Travel calculations use `trips` and `measurements`, linked through `measurements.trip_id`.

When the user changes language, the application updates both its ResourceBundle labels and its database unit names and descriptions. The DAO uses a parameterized query with a `LEFT JOIN` and `COALESCE` to fall back to English when a requested translation is missing. The GUI displays these names in the conversion selector, results and latest five conversions, with descriptions alongside the selector and in travel tooltips.

This approach fits the project because each unit keeps one stable identifier across all languages. Calculations reference that identifier, and translation edits can be made independently of saved measurements. Adding another database language requires new translation rows while retaining the schema. The main tradeoff is an additional join when loading the catalog. For six units and five languages, that query remains small and straightforward to maintain. Automated DAO tests verify both an unsupported language and an individually missing translation.

## Run locally

Install JDK 21 and Maven 3.9 or later.

```powershell
git clone https://github.com/Khaleddamascene/Temperature_Converter.git
cd Temperature_Converter
mvn clean verify
# In the MariaDB client, signed in with permission to create a database:
# SOURCE C:/DeskTop/Temperature_Converter/src/main/resources/init.sql;
# In PowerShell, enter a MariaDB account with read/write access to temperature_converter:
$credential = Get-Credential -Message 'MariaDB login'
$env:DB_USER = $credential.UserName
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
mvn javafx:run
```

## Run with Docker on Windows

Start Docker Desktop in Linux-container mode. Start XMing or VcXsrv on display 0 with TCP enabled. Authorize the Docker/WSL source address in the X server and permit its connection through the Windows firewall on TCP port 6000.

Use an image built from this version. Containers must be able to reach the Windows MariaDB server on port 3306, and the MariaDB account must allow connections from the container network. Set the two credential variables as shown above, then run:

```powershell
docker pull khaledmarai/temperature_converter:latest
docker run --rm --name temperature-converter -e DB_URL=jdbc:mariadb://host.docker.internal:3306/temperature_converter -e DB_USER -e DB_PASSWORD -e DISPLAY=host.docker.internal:0.0 khaledmarai/temperature_converter:latest
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

Use the complete run command with `-e DB_URL=jdbc:mariadb://host.docker.internal:3306/temperature_converter -e DB_USER -e DB_PASSWORD -e DISPLAY=host.docker.internal:0.0`. Ensure the X server is running and permits connections from Docker/WSL.

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
docker run --rm -e DB_URL=jdbc:mariadb://host.docker.internal:3306/temperature_converter -e DB_USER -e DB_PASSWORD -e DISPLAY=host.docker.internal:0.0 khaledmarai/temperature_converter:latest
```

## Kubernetes test

This project uses a Docker-based minikube profile named `temperature-demo`. Use this name consistently in all commands below.

The deployment file `k8s/deployment.yaml` runs the application with `DISPLAY=host.minikube.internal:0.0`.

### Start the cluster

Start Docker Desktop in Linux-container mode, then run:

```powershell
minikube start -p temperature-demo --driver=docker
kubectl --context=temperature-demo get nodes
```

Continue when the node shows `Ready`.

### Start and check the X server

Start XMing or VcXsrv on display 0 with TCP listening enabled. Allow connections from the cluster through the X server’s access controls and Windows firewall on port 6000.

Check the Windows connection:

```powershell
Test-NetConnection localhost -Port 6000
```

Then check from minikube:

```powershell
minikube -p temperature-demo ssh -- "nc -vz host.minikube.internal 6000"
```

Continue only when the connection succeeds. Keep the X server running.

### Build and deploy

Run these commands from the project directory:

```powershell
docker build -t khaledmarai/temperature_converter:latest .
minikube -p temperature-demo image load khaledmarai/temperature_converter:latest
kubectl --context=temperature-demo create secret generic converter-db --from-literal=DB_USER="$env:DB_USER" --from-literal=DB_PASSWORD="$env:DB_PASSWORD"
kubectl --context=temperature-demo apply -f k8s/deployment.yaml
```

The YAML uses `imagePullPolicy: IfNotPresent` to use the locally loaded image.

If updating an existing deployment after rebuilding and loading the image, restart it:

```powershell
kubectl --context=temperature-demo rollout restart deployment/temperature-converter
```

### Verify the application

```powershell
kubectl --context=temperature-demo rollout status deployment/temperature-converter --timeout=120s
kubectl --context=temperature-demo get pods -l app=temperature-converter
kubectl --context=temperature-demo logs deployment/temperature-converter
```

Confirm that the pod shows `1/1 Running` and the application window appears. Test temperature conversion and all five languages. Capture screenshots for submission.

Keep the application window open during verification. Closing it exits the application, causing Kubernetes to restart it.

### Troubleshooting

For `Unable to open DISPLAY`, check that the X server is running, port 6000 is reachable, and connections from the cluster are authorized.

If `host.minikube.internal` cannot reach the Windows X server, use a Windows IPv4 address reachable from the cluster. Test that address first, then replace the YAML’s `DISPLAY` value with `<WINDOWS-IP>:0.0` and apply the file again.

For `CrashLoopBackOff`, inspect the previous container’s logs and termination details:

```powershell
kubectl --context=temperature-demo logs deployment/temperature-converter --previous
kubectl --context=temperature-demo describe pods -l app=temperature-converter
```

A running pod alone does not confirm that the GUI works.

### Cleanup

```powershell
kubectl --context=temperature-demo delete -f k8s/deployment.yaml
minikube stop -p temperature-demo
```

## Submission

Submit the GitHub and Docker Hub links above and real screenshots of the containerized GUI running through XMing or VcXsrv. Include Jenkins, coverage and Kubernetes evidence as required. These instructions do not themselves certify successful deployment.
