import request from './request'

// 登录与管理员
export const adminLogin = (username, password) =>
  request.post(`/admin/auth/login?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`)
export const adminList = () => request.get('/admin/auth/list')
export const adminSave = (data) => request.post('/admin/auth/save', data)
export const adminDelete = (id) => request.delete(`/admin/auth/${id}`)
export const roleList = () => request.get('/admin/auth/roles')

// 用户管理
export const userList = (params) => request.get('/admin/user/list', { params })
export const userDetail = (id) => request.get(`/admin/user/detail/${id}`)
export const auditProfile = (userId, type, pass) =>
  request.post(`/admin/user/auditProfile?userId=${userId}&type=${type}&pass=${pass}`)
export const auditPhoto = (photoId, pass) =>
  request.post(`/admin/user/auditPhoto?photoId=${photoId}&pass=${pass}`)
export const limitSpeak = (userId, reason) =>
  request.post(`/admin/user/limitSpeak?userId=${userId}&reason=${encodeURIComponent(reason || '')}`)
export const banUser = (userId, reason) =>
  request.post(`/admin/user/ban?userId=${userId}&reason=${encodeURIComponent(reason || '')}`)
export const unbanUser = (userId) => request.post(`/admin/user/unban?userId=${userId}`)
export const warnUser = (userId, reason) =>
  request.post(`/admin/user/warn?userId=${userId}&reason=${encodeURIComponent(reason || '')}`)

// 认证审核
export const personalAuditList = (params) => request.get('/admin/audit/personal/list', { params })
export const personalPass = (id) => request.post(`/admin/audit/personal/pass/${id}`)
export const personalReject = (id, reason) =>
  request.post(`/admin/audit/personal/reject/${id}?reason=${encodeURIComponent(reason)}`)
export const schoolAuditList = (params) => request.get('/admin/audit/school/list', { params })
export const schoolPass = (id) => request.post(`/admin/audit/school/pass/${id}`)
export const schoolReject = (id, reason) =>
  request.post(`/admin/audit/school/reject/${id}?reason=${encodeURIComponent(reason)}`)

// 内容审核
export const postAuditList = (params) => request.get('/admin/content/post/auditList', { params })
export const postPass = (id) => request.post(`/admin/content/post/pass/${id}`)
export const postReject = (id, reason) =>
  request.post(`/admin/content/post/reject/${id}?reason=${encodeURIComponent(reason)}`)
export const commentAuditList = () => request.get('/admin/content/comment/auditList')
export const commentAudit = (id, pass) =>
  request.post(`/admin/content/comment/audit?id=${id}&pass=${pass}`)
export const reportList = (params) => request.get('/admin/content/report/list', { params })
export const handleReport = (id, status, result) =>
  request.post(`/admin/content/report/handle/${id}?status=${status}&result=${encodeURIComponent(result || '')}`)
export const hitLogList = (params) => request.get('/admin/content/hitLog', { params })

// 词库管理
export const filterList = (params) => request.get('/admin/filter/list', { params })
export const filterSave = (data) => request.post('/admin/filter/save', data)
export const filterDelete = (id) => request.delete(`/admin/filter/${id}`)
export const filterToggle = (id, enabled) =>
  request.post(`/admin/filter/toggle/${id}?enabled=${enabled}`)

// 校园广场管理
export const squareList = (params) => request.get('/admin/square/list', { params })
export const squareSetStatus = (id, status) =>
  request.post(`/admin/square/setStatus/${id}?status=${status}`)
export const squareComments = (params) => request.get('/admin/square/comments', { params })
export const deleteComment = (id) => request.delete(`/admin/square/comment/${id}`)
export const topicList = () => request.get('/admin/square/topics')
export const topicSave = (data) => request.post('/admin/square/topic/save', data)
export const topicDelete = (id) => request.delete(`/admin/square/topic/${id}`)

// 校园生活管理
export const errandList = (params) => request.get('/admin/life/errand/list', { params })
export const errandSetStatus = (id, status) =>
  request.post(`/admin/life/errand/setStatus/${id}?status=${status}`)
export const secondList = (params) => request.get('/admin/life/second/list', { params })
export const secondPass = (id) => request.post(`/admin/life/second/pass/${id}`)
export const secondSetStatus = (id, status) =>
  request.post(`/admin/life/second/setStatus/${id}?status=${status}`)

// 商城管理
export const goodsList = (params) => request.get('/admin/mall/goods/list', { params })
export const goodsSave = (data) => request.post('/admin/mall/goods/save', data)
export const goodsSetStatus = (id, status) =>
  request.post(`/admin/mall/goods/setStatus/${id}?status=${status}`)
export const goodsDelete = (id) => request.delete(`/admin/mall/goods/${id}`)
export const mallCategories = () => request.get('/admin/mall/categories')
export const categorySave = (data) => request.post('/admin/mall/category/save', data)
export const categoryDelete = (id) => request.delete(`/admin/mall/category/${id}`)
export const orderList = (params) => request.get('/admin/mall/order/list', { params })
export const orderShip = (id) => request.post(`/admin/mall/order/ship/${id}`)
export const orderRefund = (id, pass) =>
  request.post(`/admin/mall/order/refund/${id}?pass=${pass}`)

// 数据统计
export const statOverview = () => request.get('/admin/stat/overview')

// 文件上传地址
export const uploadUrl = '/api/file/upload'
