package vn.edu.tdtu.edocument.repository;

import java.nio.file.Paths;

/** [Factory Method] Chọn nơi lưu trữ theo cấu hình; nghiệp vụ không biết lớp cụ thể. */
public final class RepositoryFactory {
    private RepositoryFactory() { }

    public static DocumentRepository create(StorageType type) {
        switch (type) {
            case MYSQL: return new MySqlDocumentRepository(System.out::println);
            case S3: return new S3DocumentRepository("edocument-bucket", System.out::println);
            case LOCAL_JSON:
            default: return new LocalJsonDocumentRepository(Paths.get("server_storage"));
        }
    }
}
