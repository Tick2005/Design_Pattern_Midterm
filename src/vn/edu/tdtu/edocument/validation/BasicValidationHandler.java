package vn.edu.tdtu.edocument.validation;

import vn.edu.tdtu.edocument.model.Document;

import java.io.File;
import java.util.Locale;
import java.util.Set;

/** Trạm 1: tệp tồn tại, dung lượng tối đa, định dạng nằm trong danh sách được hỗ trợ. */
public class BasicValidationHandler extends ValidationHandler {
    private final Set<String> allowedExtensions;
    private final long maxSizeKB;

    public BasicValidationHandler(Set<String> allowedExtensions, long maxSizeKB) {
        this.allowedExtensions = allowedExtensions;
        this.maxSizeKB = maxSizeKB;
    }

    @Override
    protected void check(Document doc) throws ValidationException {
        if (!new File(doc.getFilePath()).isFile()) {
            throw new ValidationException(stationName(), "Tệp đính kèm không tồn tại: " + doc.getFilePath());
        }
        if (doc.getFileSizeKB() > maxSizeKB) {
            throw new ValidationException(stationName(),
                    "Dung lượng " + doc.getFileSizeKB() + "KB vượt quá giới hạn " + maxSizeKB + "KB");
        }
        if (!allowedExtensions.contains(doc.getFileExtension().toLowerCase(Locale.ROOT))) {
            throw new ValidationException(stationName(),
                    "Định dạng ." + doc.getFileExtension() + " không được hỗ trợ " + allowedExtensions);
        }
    }

    @Override
    public String stationName() { return "Kiểm tra cơ bản"; }
}
