import request from '@/utils/request'

export function getReviewList(params: any) {
    return request({
        url: '/admin/review/list',
        method: 'get',
        params
    })
}

export function replyReview(data: { id: number; content: string }) {
    return request({
        url: '/admin/review/reply',
        method: 'post',
        data
    })
}

export function updateReviewStatus(id: number, status: number) {
    return request({
        url: `/admin/review/status/${id}/${status}`,
        method: 'post'
    })
}
