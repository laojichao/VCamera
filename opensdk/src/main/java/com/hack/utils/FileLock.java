package com.hack.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于文件通道的排他锁管理工具。
 * <p>
 * 提供对文件的排他锁定与解锁能力，使用 {@link java.nio.channels.FileLock} 实现。
 * 内部维护引用计数机制，支持对同一锁文件的多次加锁操作，仅当引用计数归零时
 * 才真正释放文件锁并关闭相关资源。
 * </p>
 * <p>以单例模式运行，通过 {@link #getInstance()} 获取全局实例。</p>
 */
public class FileLock {
    private static FileLock singleton;
    /** 文件路径到锁计数信息的映射，使用 ConcurrentHashMap 保证线程安全 */
    private Map<String, FileLockCount> mRefCountMap = new ConcurrentHashMap<String, FileLockCount>();

    /**
     * 获取 {@link FileLock} 的单例实例。
     * <p>注意：该方法非线程安全，建议在应用初始化阶段调用。</p>
     *
     * @return {@link FileLock} 全局单例实例
     */
    public static FileLock getInstance() {
        if (singleton == null) {
            singleton = new FileLock();
        }
        return singleton;
    }

    /**
     * 增加指定文件路径的锁引用计数。
     * <p>若文件路径已存在锁记录，则递增引用计数；否则创建新的锁记录。</p>
     *
     * @param filePath         锁文件的绝对路径
     * @param fileLock         NIO 文件锁对象
     * @param randomAccessFile 随机访问文件对象
     * @param fileChannel      文件通道对象
     * @return 操作前的引用计数值（新增时为 1）
     */
    private int RefCntInc(String filePath, java.nio.channels.FileLock fileLock, RandomAccessFile randomAccessFile,
                          FileChannel fileChannel) {
        int refCount;
        if (this.mRefCountMap.containsKey(filePath)) {
            FileLockCount fileLockCount = this.mRefCountMap.get(filePath);
            int i = fileLockCount.mRefCount;
            fileLockCount.mRefCount = i + 1;
            refCount = i;
        } else {
            refCount = 1;
            this.mRefCountMap.put(filePath, new FileLockCount(fileLock, refCount, randomAccessFile, fileChannel));

        }
        return refCount;
    }

    /**
     * 减少指定文件路径的锁引用计数。
     * <p>若引用计数递减后小于等于 0，则从映射中移除该锁记录。</p>
     *
     * @param filePath 锁文件的绝对路径
     * @return 递减后的引用计数值，若锁记录不存在则返回 0
     */
    private int RefCntDec(String filePath) {
        int refCount = 0;
        if (this.mRefCountMap.containsKey(filePath)) {
            FileLockCount fileLockCount = this.mRefCountMap.get(filePath);
            int i = fileLockCount.mRefCount - 1;
            fileLockCount.mRefCount = i;
            refCount = i;
            if (refCount <= 0) {
                this.mRefCountMap.remove(filePath);
            }
        }
        return refCount;
    }

    /**
     * 对目标文件所在目录加排他锁。
     * <p>在目标文件的同级目录下创建名为 "lock" 的锁文件，并通过 NIO 文件通道
     * 获取排他锁。使用引用计数机制支持同一目录的重复加锁。</p>
     *
     * @param targetFile 需要加锁的目标文件，其父目录用于存放锁文件
     * @return {@code true} 表示加锁成功，{@code false} 表示目标文件为空或加锁异常
     */
    public boolean LockExclusive(File targetFile) {

        if (targetFile == null) {
            return false;
        }
        try {
            File lockFile = new File(targetFile.getParentFile().getAbsolutePath().concat("/lock"));
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile.getAbsolutePath(), "rw");
            FileChannel channel = randomAccessFile.getChannel();
            java.nio.channels.FileLock lock = channel.lock();
            if (!lock.isValid()) {
                return false;
            }
            RefCntInc(lockFile.getAbsolutePath(), lock, randomAccessFile, channel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 释放目标文件所在目录的排他锁。
     * <p>递减引用计数，当计数归零时释放 NIO 文件锁并关闭相关的
     * RandomAccessFile 和 FileChannel 资源。</p>
     *
     * @param targetFile 需要解锁的目标文件，其父目录下的锁文件将被释放
     */
    public void unLock(File targetFile) {

        File lockFile = new File(targetFile.getParentFile().getAbsolutePath().concat("/lock"));
        if (!lockFile.exists()) {
            return;
        }
        if (this.mRefCountMap.containsKey(lockFile.getAbsolutePath())) {
            FileLockCount fileLockCount = this.mRefCountMap.get(lockFile.getAbsolutePath());
            if (fileLockCount != null) {
                java.nio.channels.FileLock fileLock = fileLockCount.mFileLock;
                RandomAccessFile randomAccessFile = fileLockCount.fOs;
                FileChannel fileChannel = fileLockCount.fChannel;
                try {
                    if (RefCntDec(lockFile.getAbsolutePath()) <= 0) {
                        if (fileLock != null && fileLock.isValid()) {
                            fileLock.release();
                        }
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        if (fileChannel != null) {
                            fileChannel.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 文件锁计数内部类。
     * <p>封装 NIO 文件锁、随机访问文件、文件通道及引用计数信息。</p>
     */
    private class FileLockCount {
        /** NIO 文件通道 */
        FileChannel fChannel;
        /** 随机访问文件 */
        RandomAccessFile fOs;
        /** NIO 文件锁对象 */
        java.nio.channels.FileLock mFileLock;
        /** 当前锁的引用计数 */
        int mRefCount;

        /**
         * 构造锁计数对象。
         *
         * @param fileLock  NIO 文件锁
         * @param mRefCount 初始引用计数
         * @param fOs       随机访问文件
         * @param fChannel  文件通道
         */
        FileLockCount(java.nio.channels.FileLock fileLock, int mRefCount, RandomAccessFile fOs,
                      FileChannel fChannel) {
            this.mFileLock = fileLock;
            this.mRefCount = mRefCount;
            this.fOs = fOs;
            this.fChannel = fChannel;
        }
    }
}
