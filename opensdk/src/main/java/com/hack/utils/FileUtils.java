package com.hack.utils;

import android.content.Context;

import com.hack.Features;
import com.hack.Slog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 文件操作工具类。
 * <p>
 * 提供常用的文件操作方法，包括流拷贝、资源提取、ZIP 文件解压、
 * 文件/目录删除、字符串读写等。所有方法均为静态方法，可直接调用。
 * </p>
 */
public class FileUtils {
    private static final boolean DEBUG = Features.DEBUG;
    /** 流拷贝缓冲区大小 */
    private static final int FILE_BYTE_BUFFER = 4096;
    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * 将输入流内容拷贝到输出流。
     * <p>使用 4096 字节缓冲区逐块读取并写入，调用方需自行关闭流。</p>
     *
     * @param in  输入流，不为 {@code null}
     * @param out 输出流，不为 {@code null}
     * @return 实际拷贝的字节总数
     * @throws IOException 若读取或写入过程中发生 I/O 错误
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        int total = 0;
        byte[] buffer = new byte[FILE_BYTE_BUFFER];
        int c;
        while ((c = in.read(buffer)) != -1) {
            total += c;
            out.write(buffer, 0, c);
        }
        return total;
    }

    /**
     * 将输入流内容拷贝到输出流，并在完成后自动关闭两个流。
     * <p>无论拷贝是否成功，都会在 {@code finally} 块中关闭输入流和输出流。</p>
     *
     * @param in  输入流，不为 {@code null}
     * @param out 输出流，不为 {@code null}
     * @return 实际拷贝的字节总数
     * @throws IOException 若读取或写入过程中发生 I/O 错误
     */
    public static int copyAndClose(InputStream in, OutputStream out) throws IOException {
        try {
            int total = 0;
            byte[] buffer = new byte[FILE_BYTE_BUFFER];
            int c;
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            return total;
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }

    }

    /**
     * 静默关闭可关闭资源。
     * <p>若关闭过程中发生 IOException，将被静默忽略。</p>
     *
     * @param closeable 待关闭的资源，允许为 {@code null}
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    /**
     * 从 APK 的 assets 目录提取资源文件到指定目标路径。
     * <p>自动创建目标文件的父目录。</p>
     *
     * @param context 应用上下文，用于访问 assets 资源
     * @param name    assets 中的资源文件名
     * @param target  目标文件路径
     * @throws IOException 若读取资源或写入目标文件失败
     */
    public static void extractAsset(Context context, String name, File target) throws IOException {
        target.getParentFile().mkdirs();
        copyAndClose(context.getAssets().open(name), new FileOutputStream(target));
    }

    /**
     * 从 ZIP 文件中提取指定目录下的所有文件到输出目录。
     * <p>执行路径遍历安全检查（Zip Slip 防护），确保解压目标路径不会逃逸出
     * 输出目录。自动跳过 ZIP 中的目录条目。</p>
     *
     * @param file   ZIP 源文件
     * @param dir    ZIP 内需要提取的目录前缀
     * @param output 输出目标目录
     * @throws IOException 若读取 ZIP 文件、路径安全检查失败或写入目标文件失败
     */
    public static void extractFile(File file, String dir, File output) throws IOException {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            String DIR = output.getCanonicalPath();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith(dir)) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    File target = new File(output, entry.getName());
                    String canonicalPath = target.getCanonicalPath();
                    if (!canonicalPath.startsWith(DIR)) {
                        throw new IOException("security path " + entry.getName());
                    }
                    target.getParentFile().mkdirs();
                    copyAndClose(zipFile.getInputStream(entry), new FileOutputStream(target));
                }
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    /**
     * 静默删除文件或目录。
     * <p>若目标为目录，则递归删除其下所有子文件和子目录。
     * 删除过程中发生的任何异常均被静默忽略。</p>
     *
     * @param scratch 待删除的文件或目录
     * @return {@code true} 表示删除成功，{@code false} 表示删除失败
     */
    public static boolean deleteQuietly(File scratch) {
        try {
            if (!scratch.isFile()) {
                File[] files = scratch.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteQuietly(file);
                    }
                }
            }
            return scratch.delete();
        } catch (Exception e) {

        }
        return false;

    }

    /**
     * 以 UTF-8 编码读取文件内容为字符串。
     *
     * @param file 待读取的文件
     * @return 文件内容字符串，若读取失败则返回 {@code null}
     */
    public static String readString(File file) {
        return readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 以指定字符集读取文件内容为字符串。
     *
     * @param file    待读取的文件
     * @param charset 字符编码，若为 {@code null} 则使用平台默认编码
     * @return 文件内容字符串，若读取失败则返回 {@code null}
     */
    public static String readString(File file, Charset charset) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            String result = readString(inputStream, charset);
            inputStream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从输入流中读取全部内容并转换为字符串。
     *
     * @param inputStream 输入流
     * @param charset     字符编码，若为 {@code null} 则使用平台默认编码
     * @return 流内容字符串，若读取失败则返回 {@code null}
     */
    public static String readString(InputStream inputStream, Charset charset) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            FileUtils.copy(inputStream, os);
            if (charset != null) {
                return new String(os.toByteArray(), charset);
            }
            return os.toString();
        } catch (Throwable e) {
            //ignore
        }
        return null;

    }

    /**
     * 将字符串内容写入文件。
     * <p>自动创建目标文件的父目录，使用 UTF-8 编码写入。
     * 写入完成后自动关闭输出流。</p>
     *
     * @param file 目标文件
     * @param json 待写入的字符串内容
     * @throws IOException 若创建目录或写入文件失败
     */
    public static void writeString(File file, String json) throws IOException {
        FileOutputStream outputStream = null;
        try {
            file.getParentFile().mkdirs();
            outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
        } finally {
            closeQuietly(outputStream);
        }
    }
}
