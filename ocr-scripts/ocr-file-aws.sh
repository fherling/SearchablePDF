#!/bin/bash
echo "OCR-input $1"

inputfile="$1"

stat -c %w "$inputfile"


if [  -f "$inputfile" ]; then

    creation_timestamp=$(stat -c %w "$inputfile")
    creation_date=$(date -d "$creation_timestamp" '+%Y-%m-%d')

    echo "Creation date of $inputfile: $creation_date"


    outputfilename=$(basename "$inputfile")
    outputdirectory=$(dirname "$inputfile")
    outputdirectory="${outputdirectory//ocr-input/ocr-output}"
    outputdirectory="$outputdirectory/aws/$creation_date"
    mkdir -p "$outputdirectory"
    outputfile=$outputdirectory/$outputfilename
    echo "OCR-output $outputfile"

    mimetype=$(file --mime-type -b "$inputfile")
    echo "Mimetype: ${mimetype}"
    if [ "$mimetype" = "application/pdf" ]; then
            java -cp searchable-pdf-1.0.jar OcrRunner "$inputfile" "$outputfile"

            echo 'OCR complete'
    else
            echo 'Wrong file type. OCR skipped'
    fi
else
    echo 'Input is a directory. OCR skipped'    
fi
