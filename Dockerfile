FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src/ src/
RUN mkdir classes && javac -encoding UTF-8 --release 17 -d classes src/*.java

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/classes/ classes/
COPY web/ web/
CMD ["java", "-cp", "classes", "FitnessApiServer"]
