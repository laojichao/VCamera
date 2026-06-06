package com.hack.agent;

import static com.hack.opensdk.CmdConstants.CMD_FILE_PROVIDER_MAKE_URI;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.hack.opensdk.Cmd;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 代理文件 ContentProvider 基类。
 * <p>
 * 作为所有文件代理 Provider 的基础实现，将 ContentProvider 的各项操作
 * （query、insert、update、delete、openFile 等）通过 {@link Cmd} 命令机制
 * 委托给底层引擎，由引擎完成 URI 的实际转换与目标 Provider 的访问。
 * </p>
 * <p>
 * 所有操作的核心流程为：
 * <ol>
 *   <li>通过 {@code CMD_FILE_PROVIDER_MAKE_URI} 将代理 URI 转换为目标 URI</li>
 *   <li>获取目标 URI 对应的 {@link ContentProviderClient}</li>
 *   <li>通过 Client 将操作转发给实际的 ContentProvider</li>
 * </ol>
 * </p>
 */
public class BaseAgentFileProvider extends ContentProvider {
    private static final String TAG ="proxies." + BaseAgentFileProvider.class.getSimpleName();

    /**
     * ContentProvider 创建时调用。
     *
     * @return 始终返回 {@code false}，表示延迟初始化
     */
    @Override
    public boolean onCreate() {
        return false;
    }

    /**
     * 获取指定 URI 对应的 ContentProvider 客户端。
     *
     * @param context 应用上下文
     * @param uri     目标 ContentProvider 的 URI
     * @return 对应的 {@link ContentProviderClient}，获取失败时可能返回 {@code null}
     */
    public static ContentProviderClient getClient(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.acquireContentProviderClient(uri.getAuthority());
    }

    /**
     * 查询数据。
     * <p>
     * 将代理 URI 转换为目标 URI 后，通过目标 ContentProvider 执行查询。
     * </p>
     *
     * @param uri           代理 URI
     * @param projection    需要返回的列名数组，{@code null} 表示返回所有列
     * @param selection     过滤条件（WHERE 子句，不含 WHERE 关键字）
     * @param selectionArgs  selection 中占位符 {@code ?} 的参数值
     * @param sortOrder     排序方式（ORDER BY 子句，不含 ORDER BY 关键字）
     * @return 查询结果的 {@link Cursor}，失败时返回 {@code null}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.query(proxyOrTarget, projection, selection, selectionArgs, sortOrder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取指定 URI 对应数据的 MIME 类型。
     *
     * @param uri 代理 URI
     * @return MIME 类型字符串，失败时返回 {@code null}
     */
    @Override
    public String getType(Uri uri) {
        Log.e(TAG, "getType: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.getType(proxyOrTarget);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 插入数据。
     *
     * @param uri    代理 URI
     * @param values 要插入的数据键值对
     * @return 插入成功后新记录的 URI，失败时返回 {@code null}
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.insert(proxyOrTarget, values);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 删除数据。
     *
     * @param uri           代理 URI
     * @param selection     过滤条件
     * @param selectionArgs  selection 中占位符的参数值
     * @return 被删除的行数，失败时返回 {@code 0}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.delete(proxyOrTarget, selection, selectionArgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 更新数据。
     *
     * @param uri           代理 URI
     * @param values        要更新的数据键值对
     * @param selection     过滤条件
     * @param selectionArgs  selection 中占位符的参数值
     * @return 被更新的行数，失败时返回 {@code 0}
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.e(TAG, "update: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.update(proxyOrTarget, values, selection, selectionArgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 批量插入数据。
     *
     * @param uri    代理 URI
     * @param values 要批量插入的数据数组
     * @return 成功插入的行数，失败时返回 {@code 0}
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.e(TAG, "bulkInsert: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.bulkInsert(proxyOrTarget, values);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 以 Asset 方式打开文件。
     *
     * @param uri 代理 URI
     * @param str 文件打开模式（如 "r"、"w" 等）
     * @return {@link AssetFileDescriptor} 文件描述符
     * @throws FileNotFoundException 当文件不存在或无法打开时抛出
     */
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String str)
            throws FileNotFoundException {
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        Log.e(TAG, "openAssetFile " + uri + ", str " + str + "->" + proxyOrTarget);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        Log.e(TAG, "openAssetFile: client result "+client );
        if (client != null) {
            try {
                AssetFileDescriptor descriptor =  client.openAssetFile(proxyOrTarget, str);
                Log.e(TAG, "openAssetFile  result " + uri + ", str " + str + "->" + descriptor);
                return descriptor;
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "openAssetFile  result "+uri ,e);
                throw new FileNotFoundException(uri.toString());


            }
        }
        return super.openAssetFile(uri, str);
    }

    /**
     * 以 Asset 方式打开文件（支持取消信号）。
     *
     * @param uri               代理 URI
     * @param str               文件打开模式
     * @param cancellationSignal 取消信号，可用于中断正在进行的操作
     * @return {@link AssetFileDescriptor} 文件描述符
     * @throws FileNotFoundException 当文件不存在或无法打开时抛出
     */
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String str, CancellationSignal cancellationSignal)
            throws FileNotFoundException {
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        Log.e(TAG, "openAssetFile 2" + uri + ", str " + str + ", cancellationSignal " + cancellationSignal + "->" + proxyOrTarget);

        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                AssetFileDescriptor descriptor = client.openAssetFile(proxyOrTarget, str, cancellationSignal);
                Log.e(TAG, "openAssetFile 2  result " + proxyOrTarget + ", str " + str + "->" + client.getLocalContentProvider());
                return descriptor;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.openAssetFile(uri, str, cancellationSignal);
    }

    /**
     * 打开文件。
     *
     * @param uri 代理 URI
     * @param str 文件打开模式
     * @return {@link ParcelFileDescriptor} 文件描述符
     * @throws FileNotFoundException 当文件不存在或无法打开时抛出
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String str)
            throws FileNotFoundException {
        Log.e(TAG, "openFile: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.openFile(proxyOrTarget, str);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.openFile(uri, str);
    }

    /**
     * 打开文件（支持取消信号）。
     *
     * @param uri               代理 URI
     * @param str               文件打开模式
     * @param cancellationSignal 取消信号
     * @return {@link ParcelFileDescriptor} 文件描述符
     * @throws FileNotFoundException 当文件不存在或无法打开时抛出
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String str, CancellationSignal cancellationSignal)
            throws FileNotFoundException {
        Log.e(TAG, "openFile: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.openFile(proxyOrTarget, str, cancellationSignal);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.openFile(uri, str, cancellationSignal);
    }

    /**
     * 以指定 MIME 类型过滤方式打开 Asset 文件。
     *
     * @param uri            代理 URI
     * @param mimeTypeFilter MIME 类型过滤器
     * @param opts           附加选项
     * @return {@link AssetFileDescriptor} 文件描述符
     * @throws FileNotFoundException 当文件不存在或无法打开时抛出
     */
    @Override
    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts)
            throws FileNotFoundException {
        Log.e(TAG, "openTypedAssetFile: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.openTypedAssetFileDescriptor(proxyOrTarget, mimeTypeFilter, opts);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
    }

    /**
     * 以指定 MIME 类型过滤方式打开 Asset 文件（支持取消信号）。
     *
     * @param uri               代理 URI
     * @param mimeTypeFilter    MIME 类型过滤器
     * @param opts              附加选项
     * @param cancellationSignal 取消信号
     * @return {@link AssetFileDescriptor} 文件描述符
     * @throws FileNotFoundException 当文件不存在或无法打开时抛出
     */
    @Override
    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts,
                                                  CancellationSignal cancellationSignal)
            throws FileNotFoundException {
        Log.e(TAG, "openTypedAssetFile: "+uri );
        Uri proxyOrTarget = (Uri) Cmd.INSTANCE().exec(CMD_FILE_PROVIDER_MAKE_URI, uri);
        ContentProviderClient client = getClient(getContext(), proxyOrTarget);
        if (client != null) {
            try {
                return client.openTypedAssetFile(proxyOrTarget, mimeTypeFilter, opts, cancellationSignal);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.openTypedAssetFile(uri, mimeTypeFilter, opts, cancellationSignal);
    }

    /**
     * 批量执行 ContentProvider 操作。
     *
     * @param operations 要执行的操作列表
     * @return 每个操作的执行结果数组
     * @throws OperationApplicationException 当操作执行失败时抛出
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        Log.e(TAG, "applyBatch: " );
        if (operations != null) {
            Log.d(TAG, "applyBatch operations " + Arrays.toString(operations.toArray()));
        }
        return super.applyBatch(operations);
    }
}
