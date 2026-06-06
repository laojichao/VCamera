package com.hack.agent;

/**
 * 应用代理文件提供者。
 * <p>
 * 继承自 {@link BaseAgentFileProvider}，作为应用级文件代理 ContentProvider 的具体实现。
 * 该类本身无额外逻辑，主要用于在 AndroidManifest 中声明独立的 authority，
 * 以支持多实例文件代理场景。
 * </p>
 */
public class AppAgentFileProvider extends BaseAgentFileProvider {
}
