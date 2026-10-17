package vn.edu.tdtu.edocument.extraction;

/** [Strategy] Một cách đọc nội dung tệp đính kèm. */
public interface ContentExtractor {
    String extract(String filePath) throws ExtractionException;
}
