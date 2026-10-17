package vn.edu.tdtu.edocument.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * [Builder] Lắp ráp hồ sơ theo từng bước.
 * - buildDraft(): không bắt buộc trường nào  -> dùng cho "Lưu nháp".
 * - build()     : kiểm tra đủ trường bắt buộc -> dùng cho "Nộp hồ sơ".
 */
public class DocumentBuilder {
    private String id = UUID.randomUUID().toString().substring(0, 8);
    private String applicantName, applicantEmail, applicantPhone;
    private String officerName, officerEmail, officerPhone;
    private String documentType;
    private String filePath, fileExtension;
    private long fileSizeKB;
    private String digitalSignature;
    private String extractedContent, fileHash;
    private DocumentStatus status = DocumentStatus.DRAFT;

    /** Tiếp tục chỉnh sửa một hồ sơ đã lưu (bản nháp). */
    public static DocumentBuilder from(Document d) {
        DocumentBuilder b = new DocumentBuilder();
        b.id = d.getId();
        b.applicant(d.getApplicantName(), d.getApplicantEmail(), d.getApplicantPhone());
        b.officer(d.getOfficerName(), d.getOfficerEmail(), d.getOfficerPhone());
        b.documentType = d.getDocumentType();
        b.filePath = d.getFilePath();
        b.fileExtension = d.getFileExtension();
        b.fileSizeKB = d.getFileSizeKB();
        b.digitalSignature = d.getDigitalSignature();
        b.extractedContent = d.getExtractedContent();
        b.fileHash = d.getFileHash();
        b.status = d.getStatus();
        return b;
    }

    // ---- Bước 1: thông tin cá nhân ----
    public DocumentBuilder id(String id) { this.id = id; return this; }

    public DocumentBuilder applicant(String name, String email, String phone) {
        this.applicantName = name; this.applicantEmail = email; this.applicantPhone = phone;
        return this;
    }

    public DocumentBuilder officer(String name, String email, String phone) {
        this.officerName = name; this.officerEmail = email; this.officerPhone = phone;
        return this;
    }

    public DocumentBuilder documentType(String type) { this.documentType = type; return this; }

    // ---- Bước 2: tài liệu ----
    /** Tự suy ra đuôi file và dung lượng từ tệp thật. */
    public DocumentBuilder attachment(String path) {
        File f = new File(path);
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        String ext = dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
        long kb = f.isFile() ? (f.length() + 1023) / 1024 : 0;
        return attachment(path, ext, kb);
    }

    public DocumentBuilder attachment(String path, String extension, long sizeKB) {
        this.filePath = path; this.fileExtension = extension; this.fileSizeKB = sizeKB;
        return this;
    }

    public DocumentBuilder digitalSignature(String signature) { this.digitalSignature = signature; return this; }

    // ---- Bước 3: dựng đối tượng ----
    public Document buildDraft() {
        return create();
    }

    public Document build() throws IncompleteDocumentException {
        List<String> problems = new ArrayList<>();
        require(applicantName, "Tên người nộp", problems);
        require(applicantEmail, "Email người nộp", problems);
        require(applicantPhone, "SĐT người nộp", problems);
        require(officerName, "Tên cán bộ", problems);
        require(officerEmail, "Email cán bộ", problems);
        require(officerPhone, "SĐT cán bộ", problems);
        require(documentType, "Loại hồ sơ", problems);
        require(filePath, "Tệp đính kèm", problems);
        require(fileExtension, "Đuôi tệp", problems);
        require(digitalSignature, "Chữ ký số", problems);
        if (isFilled(applicantEmail) && !applicantEmail.contains("@")) {
            problems.add("Email người nộp sai định dạng (thiếu '@')");
        }
        if (!problems.isEmpty()) throw new IncompleteDocumentException(problems);
        return create();
    }

    private Document create() {
        return new Document(id, applicantName, applicantEmail, applicantPhone,
                officerName, officerEmail, officerPhone, documentType, filePath,
                fileExtension, fileSizeKB, digitalSignature, extractedContent, fileHash, status);
    }

    private static boolean isFilled(String s) { return s != null && !s.trim().isEmpty(); }

    private static void require(String value, String label, List<String> problems) {
        if (!isFilled(value)) problems.add("Thiếu " + label);
    }
}
