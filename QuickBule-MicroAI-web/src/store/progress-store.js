import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useProgressStore = defineStore('progress', () => {
    // 票券生成进度映射
    const progressMap = ref({});

    // 二维码生成进度映射
    const qrProgressMap = ref({});

    // 获取票券生成进度
    const getProgress = (batchId) => {
        return progressMap.value[batchId] || null;
    };

    // 更新票券生成进度
    const updateProgress = (batchId, progressData) => {
        progressMap.value[batchId] = progressData;
    };

    // 获取二维码生成进度
    const getQRProgress = (batchId) => {
        return qrProgressMap.value[batchId] || null;
    };

    // 更新二维码生成进度
    const updateQRProgress = (batchId, progressData) => {
        qrProgressMap.value[batchId] = progressData;
    };

    // 清除进度
    const clearProgress = (batchId) => {
        delete progressMap.value[batchId];
    };

    // 清除二维码进度
    const clearQRProgress = (batchId) => {
        delete qrProgressMap.value[batchId];
    };

    return {
        progressMap,
        qrProgressMap,
        getProgress,
        updateProgress,
        getQRProgress,
        updateQRProgress,
        clearProgress,
        clearQRProgress,
    };
});
