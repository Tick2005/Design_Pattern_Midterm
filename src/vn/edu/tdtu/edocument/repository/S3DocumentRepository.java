package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.model.Document;

import java.util.function.Consumer;

/** Giả lập AWS S3: không kết nối thật, chỉ log thao tác PUT object. */
public class S3DocumentRepository extends InMemoryDocumentRepository {
    private final String bucket;
    private final Consumer<String> log;

    public S3DocumentRepository(String bucket, Consumer<String> log) {
        this.bucket = bucket;
        this.log = log;
    }

    @Override
    public void save(Document d) {
        log.accept("  [S3] PUT s3://" + bucket + "/documents/" + d.getId() + ".json  (status=" + d.getStatus() + ")");
        super.save(d);
    }
}
