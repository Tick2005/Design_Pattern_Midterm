package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.model.Document;

import java.util.function.Consumer;

/** Giả lập MySQL: không kết nối thật, chỉ log câu lệnh SQL sẽ chạy. */
public class MySqlDocumentRepository extends InMemoryDocumentRepository {
    private final Consumer<String> log;

    public MySqlDocumentRepository(Consumer<String> log) { this.log = log; }

    @Override
    public void save(Document d) {
        log.accept("  [MySQL] INSERT INTO documents(id, applicant, type, status, file_hash) VALUES('"
                + d.getId() + "','" + d.getApplicantName() + "','" + d.getDocumentType() + "','"
                + d.getStatus() + "','" + d.getFileHash() + "') ON DUPLICATE KEY UPDATE status=VALUES(status)");
        super.save(d);
    }
}
