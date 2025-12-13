
USED_REGION=eu-central-1

mvn clean package


docker build -f Dockerfile -t fherlingatpd/fherling-searchablepdf:latest .
#docker buildx build --platform linux/amd64,linux/arm64/v8  -f Dockerfile -t fherlingatpd/fherling-searchablepdf:latest .

