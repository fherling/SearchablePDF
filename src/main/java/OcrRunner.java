import de.fherling.ocr.OcrPdfFromLocalPdf;

public class OcrRunner {
    public static void main(String[] args) {
        try {
            String inputfile = args[0];
            String outputfile = args[1];

            System.out.println("Input file: " + inputfile);
            System.out.println("Output file: " + outputfile);

           //Generate searchable PDF from local pdf
            OcrPdfFromLocalPdf localPdf = new OcrPdfFromLocalPdf();
            localPdf.doOcr(inputfile, outputfile);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
