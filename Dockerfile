FROM maven:alpine
WORKDIR /code
COPY . /code 

ARG DB_HOST
ARG DB_PASSWORD
ARG DB_USER
ARG DB_NAME
ARG JWT_SECRET

ENV DB_HOST ${DB_HOST}
ENV DB_PASSWORD ${DB_PASSWORD}
ENV DB_USER ${DB_USER}
ENV DB_NAME ${DB_NAME}
ENV JWT_SECRET ${JWT_SECRET}

RUN mvn clean install -DskipTests=true -Dmaven.javadoc.skip=true

EXPOSE 8080

CMD ["java","-jar", "/code/target/moves-like-swagger-api-1.0-SNAPSHOT.jar", "server", "/code/config.yml"]