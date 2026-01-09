import request from '@/utils/request'

// [通用] 获取分类列表
export function getCategoryList(params?: any) {
    return request({
        url: '/category/list',
        method: 'get',
        params
    })
}

// [管理端] 获取分类树 (核心接口)
export function getCategoryTree() {
    return request({
        url: '/category/admin/tree',
        method: 'get'
    })
}

// [管理端] 新增分类
export function addCategory(data: any) {
    return request({
        url: '/category/admin/add',
        method: 'post',
        data
    })
}

// [管理端] 修改分类
export function updateCategory(data: any) {
    return request({
        url: '/category/admin/update',
        method: 'put',
        data
    })
}

// [管理端] 删除分类
export function deleteCategory(id: number) {
    return request({
        url: `/category/admin/delete/${id}`,
        method: 'delete'
    })
}