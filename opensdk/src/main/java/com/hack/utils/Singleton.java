package com.hack.utils;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 延迟初始化的单例辅助抽象类。
 * <p>
 * 模仿 Android 框架中 {@code frameworks/base/include/utils/Singleton.h} 的设计模式。
 * 子类只需实现 {@link #create()} 方法来提供实例的创建逻辑，
 * 调用方通过 {@link #get()} 方法获取实例，首次调用时会触发创建。
 * 内部使用 {@code synchronized} 保证线程安全。
 * </p>
 *
 * @param <T> 单例持有的对象类型
 */
public abstract class Singleton<T> {
    /**
     * 默认构造方法。
     */
    public Singleton() {
    }

    /** 懒加载的实例缓存 */
    private T mInstance;

    /**
     * 创建单例实例的抽象方法。
     * <p>子类必须实现此方法，返回具体的实例对象。
     * 该方法仅在首次调用 {@link #get()} 时被调用一次。</p>
     *
     * @return 新创建的实例对象
     */
    protected abstract T create();

    /**
     * 获取单例实例。
     * <p>首次调用时通过 {@link #create()} 创建实例并缓存，
     * 后续调用直接返回缓存的实例。使用 {@code synchronized} 保证线程安全。</p>
     *
     * @return 单例实例对象
     */
    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}