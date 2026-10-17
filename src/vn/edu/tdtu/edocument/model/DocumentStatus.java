package vn.edu.tdtu.edocument.model;

/** Vòng đời của một hồ sơ. */
public enum DocumentStatus {
    DRAFT("Bản nháp"),
    RECEIVED("Đã tiếp nhận"),
    REJECTED("Từ chối"),
    PROCESSED("Đã xử lý");

    private final String label;

    DocumentStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
