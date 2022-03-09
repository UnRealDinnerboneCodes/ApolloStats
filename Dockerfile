FROM gradle:7.3.3-jdk17 as builder

WORKDIR /build

COPY build.gradle /build
COPY gradle.properties /build
COPY src /build/src

RUN gradle shadowJar

FROM openjdk:17-alpine
COPY --from=builder "/build/build/libs/build-1.0.0-all.jar" "ApolloStats-1.0.0-all.jar"

CMD ["java", "-jar", "ApolloStats-1.0.0-all.jar"]