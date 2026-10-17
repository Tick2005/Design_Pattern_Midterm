package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentStatus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Lưu mỗi hồ sơ thành một tệp {id}_data.json (giữ cách làm của v1.0) và sao chép tệp đính kèm. */
public class LocalJsonDocumentRepository implements DocumentRepository {
    private final Path dir;

    public LocalJsonDocumentRepository(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void save(Document d) {
        try {
            Files.write(file(d.getId()), DocumentJsonMapper.toJson(d).getBytes(StandardCharsets.UTF_8));
            Path src = d.getFilePath() == null ? null : Paths.get(d.getFilePath());
            if (src != null && Files.isRegularFile(src)) {
                Files.copy(src, dir.resolve(d.getId() + "_" + src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Lỗi lưu trữ vật lý: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Document> findById(String id) {
        Path f = file(id);
        return Files.exists(f) ? Optional.of(read(f)) : Optional.empty();
    }

    @Override
    public List<Document> findAll() {
        List<Document> all = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*_data.json")) {
            for (Path p : ds) all.add(read(p));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return all;
    }

    @Override
    public boolean existsProcessedByFileHash(String fileHash, String excludeDocumentId) {
        if (fileHash == null) return false;
        for (Document d : findAll()) {
            if (!d.getId().equals(excludeDocumentId) && d.getStatus() == DocumentStatus.PROCESSED
                    && fileHash.equals(d.getFileHash())) {
                return true;
            }
        }
        return false;
    }

    private Path file(String id) { return dir.resolve(id + "_data.json"); }

    private Document read(Path p) {
        try {
            return DocumentJsonMapper.fromJson(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
