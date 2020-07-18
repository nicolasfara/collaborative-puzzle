FROM openjdk:11

ENV VERTICLE_FILE puzzle-service-1.0.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

# Copy your fat jar to the container
COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/
COPY ./wait-for-it.sh $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["./wait-for-it.sh rabbitmq:5672 -- sleep 5 && java -jar $VERTICLE_FILE"]
