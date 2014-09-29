package com.mtvi.cfit.util;

import com.google.common.base.Joiner;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * IOUtils.
 *
 * @author zenind
 */
public final class IOUtils {

    /** Logger. */
    private static final Logger LOG  = LoggerFactory.getLogger(IOUtils.class);

    /** Hidden constructor.*/
    private IOUtils() { }

    /**
     * Handles close operation for writer.
     *
     * @param writer - writer instance.
     */
    @SuppressWarnings("all")
    public static void closeWriter(Writer writer) {
        if (writer != null) {
            Flushables.flushQuietly(writer);
            Closeables.closeQuietly(writer);
        }
    }

    /**
     * Copy source file to target directory.
     * @param targetDir - target directory.
     * @param source - source file.
     */
    public static void copy(File targetDir, File source) {
        try {
            File toReportFile = new File(targetDir, source.getName());

            Files.createParentDirs(toReportFile);
            Files.touch(toReportFile);
            Files.copy(source, toReportFile);
        } catch (IOException e) {
            LOG.error("Error during copying source file {} to target directory {}. Reason: {}",
                    source.getAbsolutePath(), targetDir.getAbsolutePath(), e.getMessage());
        }
    }

    /**
     * Gets full content of a given file as string value.
     *
     * @param file         - source file.
     * @param skippedLines - number of lines to skip.
     * @return - file content.
     * @throws IOException in case of i/o exception.
     */
    public static String getContent(File file, int skippedLines) throws IOException {
        return getContent(file, skippedLines, System.getProperty("line.separator"));
    }

    /**
     * Gets full content of a given file as string value.
     *
     * @param file         - source file.
     * @param skippedLines - number of lines to skip.
     * @param joiner - joiner symbol.
     * @return - file content.
     * @throws IOException in case of i/o exception.
     */
    public static String getContent(File file, int skippedLines, String joiner) throws IOException {
        List<String> lines = Files.readLines(file, Charset.defaultCharset());

        return Joiner.on(joiner).join(lines.subList(skippedLines, lines.size()));
    }

    /**
     * Creates given dir, including parent dirs.
     * @param dir - target dir to create.
     * @throws IOException - i/o exception.
     */
    public static void createDir(File dir) throws IOException {
        Files.createParentDirs(dir);
    }

}
