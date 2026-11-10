/**
 * 模块十九 · app-data 统一入口
 * 领域模块：auth / profile / launch / member / notebook / collection / practice / notice / search / home
 * 实现层：impl.js（Storage 读写 + API 同步）；核心函数清单见 core.js（文档 19.2）
 */
export * from './storage-keys';
export * from './auth';
export * from './profile';
export * from './launch';
export * from './settings';
export * from './member';
export * from './collection';
export * from './notice';
export * from './search';
export * from './notebook';
export * from './practice';
export * from './review';
export * from './home';
export * from './navigation';
export { APP_DATA_CORE_FUNCTIONS } from './core';
