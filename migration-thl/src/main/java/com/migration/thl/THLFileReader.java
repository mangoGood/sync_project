package com.migration.thl;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class THLFileReader implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(THLFileReader.class);

    private FileInputStream fis;
    private ObjectInputStream ois;

    public THLFileReader(String thlFile) throws IOException {
        File file = new File(thlFile);
        if (!file.exists()) {
            throw new IOException("THL file not found: " + thlFile);
        }

        fis = new FileInputStream(file);
        ois = new ObjectInputStream(fis);

        logger.info("Opened THL file: {}", thlFile);
    }

    /**
     * 子类专用构造函数，跳过文件头读取初始化。
     * 子类需自行管理 FileInputStream 的创建与读取。
     */
    protected THLFileReader(boolean skipInit) throws IOException {
        if (!skipInit) {
            throw new IllegalArgumentException("This constructor is for subclasses only");
        }
        // 不初始化 fis/ois，由子类自行管理
        this.fis = null;
        this.ois = null;
    }

    public THLEvent readEvent() throws IOException, ClassNotFoundException {
        try {
            return (THLEvent) ois.readObject();
        } catch (java.io.EOFException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if (ois != null) {
            ois.close();
        }
        if (fis != null) {
            fis.close();
        }
        logger.info("Closed THL file reader");
    }
}
