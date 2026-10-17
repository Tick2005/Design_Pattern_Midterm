package vn.edu.tdtu.edocument.model;

import java.util.List;

/** Ném ra khi nộp hồ sơ nhưng còn thiếu / sai trường bắt buộc. */
public class IncompleteDocumentException extends Exception {
    private final List<String> problems;

    public IncompleteDocumentException(List<String> problems) {
        super("Hồ sơ chưa đủ điều kiện nộp: " + String.join("; ", problems));
        this.problems = problems;
    }

    public List<String> getProblems() { return problems; }
}
