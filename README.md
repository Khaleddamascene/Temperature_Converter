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

## Assignment requirements and implementation

| Requirement | Implementation |
| --- | --- |
| Five-language JavaFX GUI | FXML interface with a language selector, temperature input, source/target conversion selector and Convert button. |
| UTF-8 interface localization | Five MessagesBundle files provide static labels. Language changes update the current controls and database text at runtime. |
| Arabic orientation | Arabic selects right-to-left layout and Arabic digits in the temperature input. |
| Database and initialization | MariaDB schema and 30 translations are defined in `src/main/resources/init.sql`. |
| Localized DAO with fallback | `TravelRepository.units(language)` returns names and descriptions with English fallback. |
| Conversion history | `conversion` stores input value, source, target, result, language and timestamp. The GUI shows the latest five entries. |
| Tests and coverage | JUnit covers conversion logic, the DAO, fallback behavior, connection settings and the GUI. JaCoCo checks core coverage during Maven verify. |
| Jenkins and Docker Hub | `Jenkinsfile` builds and tests, publishes test and coverage results, builds the image and pushes to Docker Hub. |
| X-server GUI | The Docker image includes Japanese and Arabic fonts and connects to XMing through DISPLAY. |
| Kubernetes | `k8s/deployment.yaml` deploys the application. MariaDB runs on the Windows host. |
| Localization justification | The following section explains the translation table schema and its tradeoffs. |

**Scene reload requirement:** The current implementation loads FXML at startup and updates the existing scene on language changes. Step 2 of the assignment explicitly asks for a scene reload, so this part remains a difference from the stated requirement.

## Database localization

I chose the **translation table approach** for database localization. The MariaDB database, `temperature_converter`, stores six units with names and descriptions in English, Finnish, Swedish, Japanese and Arabic. The database uses `utf8mb4`, and the UTF-8 `init.sql` script creates the tables and inserts all 30 translations.

The schema has a language-neutral `unit(id)` table containing the identifiers `celsius`, `fahrenheit`, `kelvin`, `km`, `hour` and `kmh`. The related `unit_translation(unit_id, lang, name, description)` table holds the translated text. Its composite primary key, `(unit_id, lang)`, allows one translation per unit and language. The foreign key `unit_id` references `unit.id`. The `conversion` table stores each input, source unit, target unit, result, language and timestamp. Its source and target columns reference `unit.id`. Travel calculations use `trips` and `measurements`, linked through `measurements.trip_id`.

When the user changes language, the application updates both its ResourceBundle labels and its database unit names and descriptions. The DAO uses a parameterized query with a `LEFT JOIN` and `COALESCE` to fall back to English when a requested translation is missing. The GUI displays these names in the conversion selector, results and latest five conversions, with descriptions alongside the selector and in travel tooltips.

This approach fits the project because each unit keeps one stable identifier across all languages. Calculations reference that identifier, and translation edits can be made independently of saved measurements. Adding another database language requires new translation rows while retaining the schema. The main tradeoff is an additional join when loading the catalog. For six units and five languages, that query remains small and straightforward to maintain. Automated DAO tests verify both an unsupported language and an individually missing translation.

## Run locally

Install JDK 21, Maven 3.9 or later and MariaDB. Clone the project and run the tests.

```powershell
git clone https://github.com/Khaleddamascene/Temperature_Converter.git
cd Temperature_Converter
mvn clean verify
```

Sign in to the MariaDB client with permission to create the database. Import the UTF-8 script, adjusting this absolute path for your checkout.

```sql
SOURCE C:/DeskTop/Temperature_Converter/src/main/resources/init.sql;
SELECT lang, COUNT(*) FROM temperature_converter.unit_translation GROUP BY lang;
```

The query should return six translations for each of `en`, `fi`, `sv`, `ja` and `ar`. In PowerShell, configure a MariaDB account with read/write access to the database and launch the application.

```powershell
$credential = Get-Credential -Message 'MariaDB login'
$env:DB_USER = $credential.UserName
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
$env:DB_URL = 'jdbc:mariadb://localhost:3306/temperature_converter'
mvn javafx:run
```

`DBConnection` also supports a local `database.local.properties` file containing `DB_USER` and `DB_PASSWORD` when both environment credentials are absent. This file is excluded from Git. Launch from the project folder when using it.

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

The full GUI-enabled run last verified in this project passed 35 tests. Generate a fresh report after changes before capturing submission evidence.

The assignment target is at least 80% line coverage of core logic. The Maven configuration enforces 90% per class for TemperatureConverter, NumberParser, TravelCalculator, TravelRepository and DBConnection. The DAO tests cover an unsupported language and a single missing Finnish translation, checking that English fallback preserves other Finnish translations.

Tests use isolated H2 databases in MariaDB compatibility mode. Maven supplies test credentials for GUI startup. The GUI tests exercise language switching, Arabic input, conversions, travel saving, history and database-error handling. They are disabled in the default run, so use the GUI-enabled command for controller coverage. Jenkins needs a Windows agent capable of running JavaFX.

## Jenkins configuration

The Windows pipeline requires:

- JDK 21 and a Maven tool installation named `Maven3`.
- A Windows agent for stages using `bat`.
- Docker CLI and Docker daemon access for the account running the agent.
- Docker Pipeline, JUnit and the JaCoCo plugin that provides the `jacoco()` pipeline step.
- Username/password credential ID `Temperature_Converter_ID`, with the Docker Hub username and access token.
- `DOCKERHUB_REPO = 'khaledmarai/temperature_converter'`.
- `DOCKER_IMAGE_TAG = 'latest'`.

The checked-in pipeline runs `mvn -DguiTests=true clean verify`. It publishes `target/surefire-reports/*.xml` with JUnit, runs `mvn jacoco:report`, and publishes coverage using `jacoco()`. It then builds and pushes `khaledmarai/temperature_converter:latest` using the Docker Hub credential above.

Commit and push changes before triggering Jenkins so its checkout receives the current code, tests and Dockerfile. Capture the successful pipeline and the coverage report from the same revision.

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

This project uses a Docker-based minikube profile named `temperature-homework`. Use this name consistently in all commands below.

The deployment file `k8s/deployment.yaml` runs the application with `DISPLAY=host.minikube.internal:0.0` and a MariaDB URL pointing to the Windows host on port 3306. The `converter-db` Kubernetes Secret supplies DB_USER and DB_PASSWORD. Set these environment variables in PowerShell using the login instructions above before creating the Secret. Allow the cluster's database connection in MariaDB and the Windows firewall.

### Start the cluster

Start Docker Desktop in Linux-container mode, then run:

```powershell
minikube start -p temperature-homework --driver=docker --native-ssh=true
kubectl --context=temperature-homework get nodes
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
minikube -p temperature-homework ssh -- "nc -vz host.minikube.internal 6000"
```

Continue only when the connection succeeds. Keep the X server running.

### Build and deploy

Run these commands from the project directory:

```powershell
docker build -t khaledmarai/temperature_converter:latest .
minikube -p temperature-homework image load khaledmarai/temperature_converter:latest
kubectl --context=temperature-homework create secret generic converter-db --from-literal=DB_USER="$env:DB_USER" --from-literal=DB_PASSWORD="$env:DB_PASSWORD" --dry-run=client -o yaml | kubectl --context=temperature-homework apply -f -
kubectl --context=temperature-homework apply -f k8s/deployment.yaml
```

The YAML uses `imagePullPolicy: IfNotPresent` to use the locally loaded image.

If updating an existing deployment after rebuilding and loading the image, restart it:

```powershell
kubectl --context=temperature-homework rollout restart deployment/temperature-converter
```

### Verify the application

```powershell
kubectl --context=temperature-homework rollout status deployment/temperature-converter --timeout=120s
kubectl --context=temperature-homework get pods -l app=temperature-converter
kubectl --context=temperature-homework logs deployment/temperature-converter
```

Confirm that the pod shows `1/1 Running` and the application window appears. Test temperature conversion and all five languages. Capture screenshots for submission.

Keep the application window open during verification. Closing it exits the application, causing Kubernetes to restart it.

### Troubleshooting

For `Unable to open DISPLAY`, check that the X server is running, port 6000 is reachable, and connections from the cluster are authorized.

If `host.minikube.internal` cannot reach the Windows X server, use a Windows IPv4 address reachable from the cluster. Test that address first, then replace the YAML’s `DISPLAY` value with `<WINDOWS-IP>:0.0` and apply the file again.

For `CrashLoopBackOff`, inspect the previous container’s logs and termination details:

```powershell
kubectl --context=temperature-homework logs deployment/temperature-converter --previous
kubectl --context=temperature-homework describe pods -l app=temperature-converter
```

A running pod alone does not confirm that the GUI works.

### Cleanup

```powershell
kubectl --context=temperature-homework delete -f k8s/deployment.yaml
minikube stop -p temperature-homework
```

## Submission checklist

1. Submit the [GitHub repository](https://github.com/Khaleddamascene/Temperature_Converter) with all required files committed and pushed.
2. Submit the [Docker Hub image URL](https://hub.docker.com/r/khaledmarai/temperature_converter) after publishing the corresponding image.
3. Include screenshots of the containerized GUI through XMing in English, Japanese and Arabic. Show readable text, a conversion result and the Arabic right-to-left layout.
4. Include screenshots of a successful Jenkins pipeline run, the JaCoCo report and `kubectl --context=temperature-homework get pods` showing the application as `1/1 Running`.
5. Include the half-page **Database localization** section above, describing the translation table approach, schema and reasons for choosing it.

| Required repository content | Location |
| --- | --- |
| Application code | `src/main/java/org/example/` |
| FXML and UTF-8 translations | `src/main/resources/` |
| Database schema and seed translations | `src/main/resources/init.sql` |
| Unit, DAO and GUI tests | `src/test/java/` |
| Build and coverage configuration | `pom.xml` |
| Jenkins pipeline | `Jenkinsfile` |
| Container build | `Dockerfile` |
| Kubernetes application deployment | `k8s/deployment.yaml` |
| Documentation and localization justification | `README.md` |
| Screenshots | `ScreenShots/` |

Use screenshots from the submitted version. Existing screenshots may predate the MariaDB migration or later interface changes. The temperature-homework deployment was verified on 9 November 2026 with one ready application pod and zero restarts; capture current output for submission.
