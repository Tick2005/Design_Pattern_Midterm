package vn.edu.tdtu.edocument.validation;

import vn.edu.tdtu.edocument.model.Document;

/**
 * [Chain of Responsibility + Template Method] Một trạm kiểm tra.
 * handle() chạy check() của trạm mình, đạt thì chuyển sang trạm kế tiếp; không đạt thì ném lỗi và dừng chuỗi.
 */
public abstract class ValidationHandler {
    private ValidationHandler next;

    /** Nối trạm kế tiếp, trả về trạm đó để nối liên tiếp. */
    public ValidationHandler linkWith(ValidationHandler next) {
        this.next = next;
        return next;
    }

    public final void handle(Document doc) throws ValidationException {
        check(doc);
        if (next != null) next.handle(doc);
    }

    protected abstract void check(Document doc) throws ValidationException;

    public abstract String stationName();
}
