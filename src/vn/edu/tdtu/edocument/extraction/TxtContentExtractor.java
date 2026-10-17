package vn.edu.tdtu.edocument.extraction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TxtContentExtractor implements ContentExtractor {
    @Override
    public String extract(String filePath) throws ExtractionException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExtractionException("Không thể đọc tệp TXT: " + e.getMessage(), e);
        }
    }
}
