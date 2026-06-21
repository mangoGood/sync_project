package com.migration.thl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class THLFileWriter implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(THLFileWriter.class);

    private File thlFile;
    private FileOutputStream fos;
    private ObjectOutputStream oos;

    public THLFileWriter(String filePath) throws IOException {
        this.thlFile = new File(filePath);

        File parentDir = thlFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        fos = new FileOutputStream(thlFile);
        oos = new ObjectOutputStream(fos);

        logger.info("Created THL file: {}", thlFile.getAbsolutePath());
    }

    /**
     * 子类专用构造函数，跳过 ObjectOutputStream 头写入初始化。
     * 子类需自行管理 FileOutputStream 的创建与写入。
     */
    protected THLFileWriter(boolean skipInit) throws IOException {
        if (!skipInit) {
            throw new IllegalArgumentException("This constructor is for subclasses only");
        }
        this.thlFile = null;
        this.fos = null;
        this.oos = null;
    }

    public void writeEvent(THLEvent event) throws IOException {
        oos.writeObject(event);
        oos.flush();
    }

    @Override
    public void close() throws IOException {
        if (oos != null) {
            oos.close();
        }
        if (fos != null) {
            fos.close();
        }
        logger.info("Closed THL file writer");
    }
}
