package vn.edu.tdtu.edocument.validation;

import java.io.IOException;

/** Cổng giao tiếp với phần mềm diệt virus. */
public interface VirusScanner {
    boolean isInfected(String filePath) throws IOException;
}
