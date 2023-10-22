#!/bin/bash
directory_to_watch="/ocr-input"
echo "Watching directory $directory_to_watch for new files..."

java --version

inotifywait -m -e create -e moved_to --format "%w%f" $directory_to_watch \
    | while read FILENAME 
        do  
            echo "********************"
            echo Detected new or moved file: $FILENAME
            ./ocr-file-aws.sh "$FILENAME"
            echo "********************"
        done
