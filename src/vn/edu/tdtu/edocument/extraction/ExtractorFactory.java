package vn.edu.tdtu.edocument.extraction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * [Factory + Registry] Chọn Strategy đọc tệp theo đuôi file.
 * Thêm định dạng mới = viết 1 ContentExtractor + gọi register(); không sửa mã cũ (OCP).
 */
public class ExtractorFactory {
    private final Map<String, ContentExtractor> registry = new HashMap<>();

    public static ExtractorFactory withDefaults(OcrService ocr) {
        ExtractorFactory f = new ExtractorFactory();
        f.register("txt", new TxtContentExtractor());
        f.register("pdf", new PdfContentExtractor(ocr));
        ContentExtractor image = new ImageContentExtractor(ocr);
        f.register("jpg", image);
        f.register("jpeg", image);
        f.register("png", image);
        return f;
    }

    public ExtractorFactory register(String extension, ContentExtractor extractor) {
        registry.put(extension.toLowerCase(Locale.ROOT), extractor);
        return this;
    }

    public ContentExtractor get(String extension) throws ExtractionException {
        ContentExtractor e = registry.get(extension == null ? "" : extension.toLowerCase(Locale.ROOT));
        if (e == null) throw new ExtractionException("Chưa hỗ trợ trích xuất định dạng: ." + extension);
        return e;
    }

    public Set<String> supportedExtensions() {
        return Collections.unmodifiableSet(new TreeSet<>(registry.keySet()));
    }
}
