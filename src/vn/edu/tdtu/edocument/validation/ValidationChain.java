package vn.edu.tdtu.edocument.validation;

import vn.edu.tdtu.edocument.model.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Cấu hình chuỗi kiểm duyệt: thứ tự các trạm do người dựng chuỗi quyết định.
 * Đổi thứ tự / thêm / bớt trạm chỉ là đổi danh sách truyền vào of(...).
 */
public class ValidationChain {
    private final List<ValidationHandler> stations;

    private ValidationChain(List<ValidationHandler> stations) { this.stations = stations; }

    public static ValidationChain of(ValidationHandler... handlers) {
        List<ValidationHandler> list = new ArrayList<>(Arrays.asList(handlers));
        for (int i = 0; i + 1 < list.size(); i++) list.get(i).linkWith(list.get(i + 1));
        if (!list.isEmpty()) list.get(list.size() - 1).linkWith(null);
        return new ValidationChain(list);
    }

    /** Chạy lần lượt các trạm; dừng và ném ValidationException ngay ở trạm đầu tiên thất bại. */
    public void validate(Document doc) throws ValidationException {
        if (!stations.isEmpty()) stations.get(0).handle(doc);
    }

    public List<String> stationNames() {
        List<String> names = new ArrayList<>();
        for (ValidationHandler h : stations) names.add(h.stationName());
        return Collections.unmodifiableList(names);
    }
}
