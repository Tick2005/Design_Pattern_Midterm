package vn.edu.tdtu.edocument.extraction;

public class ImageContentExtractor implements ContentExtractor {
    private final OcrService ocr;

    public ImageContentExtractor(OcrService ocr) { this.ocr = ocr; }

    @Override
    public String extract(String filePath) throws ExtractionException {
        return ocr.recognizeImage(filePath);
    }
}
