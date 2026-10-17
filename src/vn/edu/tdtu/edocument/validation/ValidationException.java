package vn.edu.tdtu.edocument.validation;

/** Lỗi của một trạm kiểm duyệt; mang theo tên trạm và lý do để báo về UI/log. */
public class ValidationException extends Exception {
    private final String station;
    private final String reason;

    public ValidationException(String station, String reason) {
        super("[" + station + "] " + reason);
        this.station = station;
        this.reason = reason;
    }

    public String getStation() { return station; }
    public String getReason() { return reason; }
}
