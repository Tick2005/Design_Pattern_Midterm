package vn.edu.tdtu.edocument.extraction;

/** Cổng giao tiếp với dịch vụ AI OCR của bên thứ ba. */
public interface OcrService {
    String recognizePdf(String filePath) throws ExtractionException;

    String recognizeImage(String filePath) throws ExtractionException;
}
