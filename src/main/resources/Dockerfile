FROM openjdk:11.0.5-jre

COPY Naurandir-Discord-Bot-0.0.1-SNAPSHOT.jar /tmp/app.jar
COPY config/official-server.p12 /tmp/official-server.p12
COPY config/application.properties /tmp/application.properties

CMD ["java", "-jar", "/tmp/app.jar", "-Dloader.main=com.discord.bot.WebAppApplication org.springframework.boot.loader.PropertiesLauncher", "--spring.config.location=/tmp/application.properties"]
