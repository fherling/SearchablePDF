#!/bin/bash

mvn clean package
docker build -f Dockerfile -t fherlingatpd/fherling-searchablepdf:latest .

