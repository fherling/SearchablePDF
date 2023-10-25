#!/bin/bash
echo "OCR-input $1"

tagesdatum=$(date +%Y-%m-%d)
inputfile="$1"


if [  -f "$inputfile" ]; then

    outputfilename=$(basename "$inputfile")
    outputdirectory=$(dirname "$inputfile")
    outputdirectory="${outputdirectory//ocr-input/ocr-output}"
    outputdirectory="$outputdirectory/$tagesdatum"
    mkdir -p "$outputdirectory"
    outputfile=$outputdirectory/$outputfilename
    echo "OCR-output $outputfile"

    mimetype=$(file --mime-type -b "$inputfile")
    echo "Mimetype: ${mimetype}"
    if [ "$mimetype" = "application/pdf" ]; then
            /usr/local/bin/ocrmypdf --clean --deskew --tesseract-timeout 600 --force-ocr -l deu "$inputfile" "$outputfile"
            echo 'OCR complete'
    else
            echo 'Wrong file type. OCR skipped'
    fi
else
    echo 'Input is a directory. OCR skipped'    
fi
