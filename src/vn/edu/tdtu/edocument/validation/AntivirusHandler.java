package vn.edu.tdtu.edocument.validation;

import vn.edu.tdtu.edocument.model.Document;

import java.io.IOException;

/** Trạm 2: quét an toàn bảo mật tệp đính kèm. */
public class AntivirusHandler extends ValidationHandler {
    private final VirusScanner scanner;

    public AntivirusHandler(VirusScanner scanner) { this.scanner = scanner; }

    @Override
    protected void check(Document doc) throws ValidationException {
        try {
            if (scanner.isInfected(doc.getFilePath())) {
                throw new ValidationException(stationName(), "Phát hiện mã độc trong tệp đính kèm");
            }
        } catch (IOException e) {
            throw new ValidationException(stationName(), "Không thể quét tệp: " + e.getMessage());
        }
    }

    @Override
    public String stationName() { return "Quét Antivirus"; }
}
