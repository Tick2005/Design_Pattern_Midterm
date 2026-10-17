package vn.edu.tdtu.edocument.validation;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.repository.DocumentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Trạm 3: chống nộp trùng bằng mã băm SHA-256 của tệp. Chỉ phụ thuộc interface DocumentRepository. */
public class DuplicateCheckHandler extends ValidationHandler {
    private final DocumentRepository repository;

    public DuplicateCheckHandler(DocumentRepository repository) { this.repository = repository; }

    @Override
    protected void check(Document doc) throws ValidationException {
        String hash = sha256(doc.getFilePath());
        if (repository.existsProcessedByFileHash(hash, doc.getId())) {
            throw new ValidationException(stationName(), "Hồ sơ trùng lặp với một hồ sơ đã được xử lý (hash " + hash.substring(0, 12) + "...)");
        }
        doc.setFileHash(hash);
    }

    private String sha256(String path) throws ValidationException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Paths.get(path)));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ValidationException(stationName(), "Không thể tính mã băm: " + e.getMessage());
        }
    }

    @Override
    public String stationName() { return "Kiểm tra trùng lặp"; }
}
