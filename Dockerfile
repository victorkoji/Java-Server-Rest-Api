FROM openjdk:alpine

COPY . /usr/src/myapp
WORKDIR /usr/src/myapp

VOLUME /usr/src/myapp
RUN javac *.java

# Inserir o comando que será executado
ENTRYPOINT ["/usr/bin/java"]

# Adicionado o que será rodado pelo ENTRYPOINT
CMD ["webserver.WebServer", "http", "-t", "8080"]