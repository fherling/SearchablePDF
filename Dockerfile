ARG ARCH=
FROM ${ARCH}openjdk:22-ea-21-jdk-slim-bullseye

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


RUN addgroup --gid 65538 ocrgroup

# Create a user 'appuser' under 'ocrgroup'
RUN adduser  --uid 1039 --gid 65538 --disabled-password --gecos "" ocruser

# Chown all the files to the app user.
RUN chown -R ocruser:ocrgroup /ocr-scripts

# Switch to 'appuser'
USER ocruser

WORKDIR /ocr-scripts


ENTRYPOINT [ "/ocr-scripts/watch-files.sh" ]