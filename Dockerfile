FROM gradle:8.7.0-jdk21 as builder

WORKDIR /build

COPY build.gradle /build
COPY gradle.properties /build
COPY src /build/src

RUN gradle shadowJar
RUN ls -l /build/build/libs/

FROM alpine/java:21
COPY --from=builder "/build/build/libs/build-1.0.0-all.jar" "ApolloStats-1.0.0-all.jar"


CMD ["java", "-jar", "ApolloStats-1.0.0-all.jar"]