FROM openjdk:alpine

COPY . /usr/src/myapp
WORKDIR /usr/src/myapp/Trabalho_Comp_2

VOLUME /usr/src/myapp

# Define variável de ambiente JAVA_HOME 
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk


# Adicionado o que será rodado pelo ENTRYPOINT
CMD ["java", "-jar", "webserver-java.jar", "http", "-t", "8080"]