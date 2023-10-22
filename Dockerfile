FROM openjdk:22-ea-17-jdk-bullseye

# Install dependencies
USER root
RUN apt update

#RUN apt install inotify-tools -y
RUN apt install curl git inotify-tools file maven -y

RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

RUN mkdir /ocr-input
RUN mkdir /ocr-output
COPY ./ocr-scripts /ocr-scripts
COPY target/searchable-pdf-1.0.jar /ocr-scripts/searchable-pdf-1.0.jar

WORKDIR /ocr-scripts

ENTRYPOINT [ "/ocr-scripts/watch-files.sh" ]