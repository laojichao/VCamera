package virtual.camera.app.cache

import android.content.Context
import android.text.TextUtils
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 基于 SharedPreferences 的属性委托基类，提供类型安全的数据读写能力。
 *
 * 目前仅支持 5 种基本数据类型：[Int]、[Long]、[Float]、[String]、[Boolean]。
 * 若需支持对象类型，可继承该类并重写 [findData] / [putData] 方法。
 *
 * @param Data 数据类型参数
 * @param context 应用上下文
 * @param default 默认值，同时用于类型推断
 * @param spName SharedPreferences 文件名，为空时使用类名作为文件名
 */
open class AppSharedPreferenceDelegate<Data>(context: Context, private val default: Data, spName: String? = null) : ReadWriteProperty<Any, Data?> {

    /** 懒加载的 SharedPreferences 实例 */
    private val mSharedPreferences by lazy {
        val tmpCacheName = if (TextUtils.isEmpty(spName)) {
            AppSharedPreferenceDelegate::class.java.simpleName
        } else {
            spName
        }
        return@lazy context.getSharedPreferences(tmpCacheName, Context.MODE_PRIVATE)
    }

    /**
     * 属性值的读取操作，从 SharedPreferences 中获取对应键的值。
     *
     * @param thisRef 属性所属对象
     * @param property 属性元数据
     * @return 存储的值，若不存在则返回 [default]
     */
    override fun getValue(thisRef: Any, property: KProperty<*>): Data {
        return findData(property.name, default)
    }

    /**
     * 属性值的写入操作，将值存入 SharedPreferences。
     *
     * @param thisRef 属性所属对象
     * @param property 属性元数据
     * @param value 要存储的值，为 null 时移除该键
     */
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Data?) {
        putData(property.name, value)
    }

    /**
     * 从 SharedPreferences 中读取指定键的值。
     *
     * 根据 [default] 的类型自动选择对应的读取方法，支持 Int、Long、Float、String、Boolean。
     *
     * @param key 键名
     * @param default 默认值
     * @return 读取到的值，若不存在则返回 [default]
     * @throws IllegalArgumentException 当数据类型不在支持范围内时抛出
     */
    protected fun findData(key: String, default: Data): Data {
        with(mSharedPreferences) {
            val result: Any = when (default) {
                is Int -> getInt(key, default)
                is Long -> getLong(key, default)
                is Float -> getFloat(key, default)
                is String -> getString(key, default)!!
                is Boolean -> getBoolean(key, default)
                else -> throw IllegalArgumentException("This type $default can not be saved into sharedPreferences")
            }
            return result as? Data ?: default
        }
    }

    /**
     * 将值写入 SharedPreferences。
     *
     * 当 [value] 为 null 时移除该键，否则根据值的类型选择对应的存储方法。
     *
     * @param key 键名
     * @param value 要存储的值，为 null 时移除
     * @throws IllegalArgumentException 当数据类型不在支持范围内时抛出
     */
    protected fun putData(key: String, value: Data?) {
        mSharedPreferences.edit {
            if (value == null) {
                remove(key)
            } else {
                when (value) {
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> throw IllegalArgumentException("This type $default can not be saved into Preferences")
                }
            }
        }
    }
}