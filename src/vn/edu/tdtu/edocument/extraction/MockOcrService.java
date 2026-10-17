package vn.edu.tdtu.edocument.extraction;

import java.io.File;

/** Giả lập dịch vụ OCR (không gọi mạng thật). */
public class MockOcrService implements OcrService {
    @Override
    public String recognizePdf(String filePath) {
        return "[OCR-PDF] Nội dung nhận diện từ " + new File(filePath).getName();
    }

    @Override
    public String recognizeImage(String filePath) {
        return "[OCR-IMG] Văn bản nhận diện từ ảnh " + new File(filePath).getName();
    }
}
