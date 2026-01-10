import request from '@/utils/request'

// --- 公告管理 ---

// 获取公告列表
export function getNoticeList(params: any) {
    return request({
        url: '/admin/notice/list',
        method: 'get',
        params
    })
}

// 保存公告 (新增或修改)
export function saveNotice(data: any) {
    return request({
        url: '/admin/notice/save',
        method: 'post',
        data
    })
}

// 删除公告
export function deleteNotice(id: number) {
    return request({
        url: `/admin/notice/delete/${id}`,
        method: 'delete'
    })
}

// --- 系统日志 ---

// 获取操作日志列表
export function getLogList(params: any) {
    return request({
        url: '/admin/system/log/list',
        method: 'get',
        params
    })
}

// 获取管理员邀请码
export function getAdminInviteCode() {
    return request({
        url: '/admin/system/invite-code',
        method: 'get'
    })
}

// --- 数据看板 ---

// 获取控制台统计数据
// [修正] 对应后端 AnalyticsController 的路径
export function getDashboardStats() {
    return request({
        url: '/system/analytics/dashboard',
        method: 'get'
    })
}
