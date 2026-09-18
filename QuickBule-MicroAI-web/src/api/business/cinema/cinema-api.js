/**
 * 影院 API (占位实现)
 *
 */

import { request } from '/@/lib/axios';

export const cinemaApi = {

    /**
     * 获取所有已分配影城的员工ID
     * 注意：此接口为占位实现，返回空数组
     */
    getAllAssignedEmployees: () => {
        console.warn('cinemaApi.getAllAssignedEmployees 为占位实现，返回空数组');
        // 使用 request 方法并直接返回正确格式的数据
        return Promise.resolve({
            ok: true,
            msg: '查询成功',
            data: []
        });
    },

    /**
     * 查询影城列表
     */
    queryCinemaList: (param) => {
        console.warn('cinemaApi.queryCinemaList 为占位实现');
        return request({
            url: '/business/cinema/query',
            method: 'post',
            data: param
        });
    },

    /**
     * 添加影城
     */
    addCinema: (param) => {
        console.warn('cinemaApi.addCinema 为占位实现');
        return request({
            url: '/business/cinema/add',
            method: 'post',
            data: param
        });
    },

    /**
     * 更新影城
     */
    updateCinema: (param) => {
        console.warn('cinemaApi.updateCinema 为占位实现');
        return request({
            url: '/business/cinema/update',
            method: 'post',
            data: param
        });
    },

    /**
     * 删除影城
     */
    deleteCinema: (cinemaId) => {
        console.warn('cinemaApi.deleteCinema 为占位实现');
        return request({
            url: `/business/cinema/delete/${cinemaId}`,
            method: 'get'
        });
    },
};
