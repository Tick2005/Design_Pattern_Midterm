package vn.edu.tdtu.edocument.model;

/**
 * Hồ sơ điện tử. Các thông tin nhập từ biểu mẫu là bất biến (final);
 * chỉ trạng thái, nội dung trích xuất và mã băm tệp thay đổi theo quy trình.
 * Chỉ được tạo thông qua {@link DocumentBuilder}.
 */
public class Document {
    private final String id;
    private final String applicantName, applicantEmail, applicantPhone;
    private final String officerName, officerEmail, officerPhone;
    private final String documentType;
    private final String filePath, fileExtension;
    private final long fileSizeKB;
    private final String digitalSignature;

    private String extractedContent;
    private String fileHash;
    private DocumentStatus status;

    Document(String id, String applicantName, String applicantEmail, String applicantPhone,
             String officerName, String officerEmail, String officerPhone,
             String documentType, String filePath, String fileExtension, long fileSizeKB,
             String digitalSignature, String extractedContent, String fileHash,
             DocumentStatus status) {
        this.id = id;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.officerName = officerName;
        this.officerEmail = officerEmail;
        this.officerPhone = officerPhone;
        this.documentType = documentType;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSizeKB = fileSizeKB;
        this.digitalSignature = digitalSignature;
        this.extractedContent = extractedContent;
        this.fileHash = fileHash;
        this.status = status;
    }

    public String getId() { return id; }
    public String getApplicantName() { return applicantName; }
    public String getApplicantEmail() { return applicantEmail; }
    public String getApplicantPhone() { return applicantPhone; }
    public String getOfficerName() { return officerName; }
    public String getOfficerEmail() { return officerEmail; }
    public String getOfficerPhone() { return officerPhone; }
    public String getDocumentType() { return documentType; }
    public String getFilePath() { return filePath; }
    public String getFileExtension() { return fileExtension; }
    public long getFileSizeKB() { return fileSizeKB; }
    public String getDigitalSignature() { return digitalSignature; }
    public String getExtractedContent() { return extractedContent; }
    public String getFileHash() { return fileHash; }
    public DocumentStatus getStatus() { return status; }

    public void setExtractedContent(String extractedContent) { this.extractedContent = extractedContent; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Hồ sơ [%s] - Nộp bởi: %s - Trạng thái: %s",
                id, applicantName, status.getLabel());
    }
}
