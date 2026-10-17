package vn.edu.tdtu.edocument.notification;

/** [Observer] Bên muốn được báo khi trạng thái hồ sơ đổi. */
public interface DocumentObserver {
    void onStatusChanged(StatusChangeEvent event);
}
