# run the docker image with local directory mounted and delete the container after exit
#mvn clean package
docker build -f Dockerfile -t fherlingatpd/fherling-searchablepdf:latest .

docker run --name fherling-searchablepdf --rm -v /Users/fherling/Documents/git/OCR/ocr-input:/ocr-input -v /Users/fherling/Documents/git/OCR/ocr-output:/ocr-output fherlingatpd/fherling-searchablepdf:latest
