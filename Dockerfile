ARG ARCH=
FROM ${ARCH}openjdk:22-ea-17-jdk-bullseye

# Install dependencies
USER root
RUN apt update


RUN apt install curl inotify-tools file unzip -y

RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-$(arch).zip" -o "awscliv2.zip"


RUN unzip awscliv2.zip
RUN ./aws/install

RUN mkdir /ocr-input
RUN mkdir /ocr-output
COPY ./ocr-scripts /ocr-scripts
COPY target/searchable-pdf-1.0.jar /ocr-scripts/searchable-pdf-1.0.jar

RUN useradd -m -u 1000 appuser
RUN chown -R appuser:appuser /ocr-scripts

WORKDIR /ocr-scripts
USER appuser


ENTRYPOINT [ "/ocr-scripts/watch-files.sh" ]