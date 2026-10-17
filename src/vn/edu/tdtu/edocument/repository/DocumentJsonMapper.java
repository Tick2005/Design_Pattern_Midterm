package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentBuilder;
import vn.edu.tdtu.edocument.model.DocumentStatus;

import java.util.Map;

/** Chuyển đổi Document <-> JSON. */
final class DocumentJsonMapper {
    private DocumentJsonMapper() { }

    static String toJson(Document d) {
        StringBuilder sb = new StringBuilder("{\n");
        field(sb, "id", d.getId(), true);
        field(sb, "applicantName", d.getApplicantName(), true);
        field(sb, "applicantEmail", d.getApplicantEmail(), true);
        field(sb, "applicantPhone", d.getApplicantPhone(), true);
        field(sb, "officerName", d.getOfficerName(), true);
        field(sb, "officerEmail", d.getOfficerEmail(), true);
        field(sb, "officerPhone", d.getOfficerPhone(), true);
        field(sb, "documentType", d.getDocumentType(), true);
        field(sb, "filePath", d.getFilePath(), true);
        field(sb, "fileExtension", d.getFileExtension(), true);
        sb.append("  \"fileSizeKB\": ").append(d.getFileSizeKB()).append(",\n");
        field(sb, "digitalSignature", d.getDigitalSignature(), true);
        field(sb, "extractedContent", d.getExtractedContent(), true);
        field(sb, "fileHash", d.getFileHash(), true);
        field(sb, "status", d.getStatus().name(), false);
        return sb.append("}").toString();
    }

    static Document fromJson(String json) {
        Map<String, String> m = JsonUtil.parseFlat(json);
        long size = m.get("fileSizeKB") == null ? 0 : Long.parseLong(m.get("fileSizeKB"));
        Document d = new DocumentBuilder()
                .id(m.get("id"))
                .applicant(m.get("applicantName"), m.get("applicantEmail"), m.get("applicantPhone"))
                .officer(m.get("officerName"), m.get("officerEmail"), m.get("officerPhone"))
                .documentType(m.get("documentType"))
                .attachment(m.get("filePath"), m.get("fileExtension"), size)
                .digitalSignature(m.get("digitalSignature"))
                .buildDraft();
        d.setExtractedContent(m.get("extractedContent"));
        d.setFileHash(m.get("fileHash"));
        d.setStatus(parseStatus(m.get("status")));
        return d;
    }

    /** Chấp nhận cả mã trạng thái cũ của v1.0. */
    private static DocumentStatus parseStatus(String s) {
        if (s == null) return DocumentStatus.DRAFT;
        switch (s) {
            case "DA_TIEP_NHAN": return DocumentStatus.RECEIVED;
            case "TU_CHOI": return DocumentStatus.REJECTED;
            case "DANG_XET_DUYET": return DocumentStatus.PROCESSED;
            default: return DocumentStatus.valueOf(s);
        }
    }

    private static void field(StringBuilder sb, String key, String value, boolean comma) {
        sb.append("  \"").append(key).append("\": ").append(JsonUtil.quote(value)).append(comma ? ",\n" : "\n");
    }
}
