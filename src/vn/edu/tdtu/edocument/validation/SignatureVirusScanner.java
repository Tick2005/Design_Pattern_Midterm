package vn.edu.tdtu.edocument.validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Bộ quét giả lập: coi tệp chứa chuỗi kiểm thử EICAR là nhiễm. */
public class SignatureVirusScanner implements VirusScanner {
    private static final String SIGNATURE = "EICAR-STANDARD-ANTIVIRUS-TEST-FILE";

    @Override
    public boolean isInfected(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.ISO_8859_1);
        return content.contains(SIGNATURE);
    }
}
