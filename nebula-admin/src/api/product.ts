import request from '@/utils/request'

// --- 文件服务 ---

// 图片上传
export function uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        // [修复] 对应后端 FileController 的 @RequestMapping("/api/file")
        url: '/file/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// --- 商品分类接口 ---

// 获取分类树 (后端 CategoryController)
export function getCategoryTree() {
    return request({
        url: '/category/admin/tree', // 对应后端 /api/category/admin/tree
        method: 'get'
    })
}

export function addCategory(data: any) {
    return request({
        url: '/category/admin/add',
        method: 'post',
        data
    })
}

export function updateCategory(data: any) {
    return request({
        url: '/category/admin/update',
        method: 'put',
        data
    })
}

export function deleteCategory(id: number) {
    return request({
        url: `/category/admin/delete/${id}`,
        method: 'delete'
    })
}

// --- 商品管理接口 ---

export function getProductList(params: any) {
    return request({
        url: '/admin/product/list',
        method: 'get',
        params
    })
}

// 获取商品详情 (用于编辑回显)
export function getProductDetail(id: number) {
    return request({
        url: `/admin/product/detail/${id}`,
        method: 'get'
    })
}

// 保存商品 (新增或修改)
// 注意：后端 ProductAdminController 需要有 @PostMapping("/save") 接口
export function saveProduct(data: any) {
    return request({
        url: '/admin/product/save',
        method: 'post',
        data
    })
}

export function updateProductStatus(ids: number[], status: number) {
    return request({
        url: `/admin/product/status/${status}`,
        method: 'post', // 修正为 POST，根据后端定义
        data: ids
    })
}

export function deleteProduct(id: number) {
    return request({
        url: `/admin/product/delete/${id}`,
        method: 'delete'
    })
}