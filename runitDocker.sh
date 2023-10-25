
mvn clean package


docker build -f Dockerfile -t fherlingatpd/fherling-searchablepdf:latest .
#docker buildx build --platform linux/amd64,linux/arm64/v8  -f Dockerfile -t fherlingatpd/fherling-searchablepdf:latest .

docker run --name fherling-searchablepdf --rm -e AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$ACCESS_KEY_SECRET -e AWS_DEFAULT_REGION=$USED_REGION -e AWS_REGION=$USED_REGION -v /Users/fherling/Documents/git/OCR/ocr-input:/ocr-input -v /Users/fherling/Documents/git/OCR/ocr-output:/ocr-output fherlingatpd/fherling-searchablepdf:latest


#docker run --name fherling-searchablepdf --rm -e AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$ACCESS_KEY_SECRET -e AWS_DEFAULT_REGION=$USED_REGION -e AWS_REGION=$USED_REGION -v /Users/fherling/Documents/git/OCR/ocr-input:/ocr-input -v /Users/fherling/Documents/git/OCR/ocr-output:/ocr-output ghcr.io/fherling/searchablepdf:latest

