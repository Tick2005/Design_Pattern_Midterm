package vn.edu.tdtu.edocument.extraction;

public class PdfContentExtractor implements ContentExtractor {
    private final OcrService ocr;

    public PdfContentExtractor(OcrService ocr) { this.ocr = ocr; }

    @Override
    public String extract(String filePath) throws ExtractionException {
        return ocr.recognizePdf(filePath);
    }
}
