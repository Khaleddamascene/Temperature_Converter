FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B clean verify


FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       libgtk-3-0 \
       libxtst6 \
       libxi6 \
       libxrender1 \
       libgl1 \
       libasound2 \
       fontconfig \
       fonts-noto-core \
       fonts-noto-cjk \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 10001 app

WORKDIR /app

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./lib

USER app

ENTRYPOINT ["java", \
    "-Djava.util.PropertyResourceBundle.encoding=UTF-8", \
    "-Dprism.order=sw", \
    "-cp", "/app/classes:/app/lib/*", \
    "org.example.Launcher"]