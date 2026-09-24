const api = require('./api')

const TYPE_TEXT = {
  warn: '平台警告',
  limit: '限制发言',
  ban: '账号封禁'
}

function typeText(type) {
  return TYPE_TEXT[type] || '平台通知'
}

function formatTime(value) {
  if (!value) return ''
  const s = String(value).replace('T', ' ')
  return s.length >= 16 ? s.slice(0, 16) : s
}

function loadUnreadCount(userId) {
  if (!userId) return Promise.resolve(0)
  return api.get('/user/violations/unread-count/' + userId).then((res) => res.count || 0).catch(() => 0)
}

function checkAndPrompt(userId) {
  if (!userId) return Promise.resolve(0)
  return loadUnreadCount(userId).then((count) => {
    if (!count) return count
    return api.get('/user/violations/' + userId).then((list) => {
      const unread = (list || []).filter((item) => !item.readStatus)
      const latest = unread[0]
      if (!latest) return count
      const lastShownId = wx.getStorageSync('lastShownViolationId') || 0
      if (latest.id <= lastShownId) return count
      wx.setStorageSync('lastShownViolationId', latest.id)
      wx.showModal({
        title: typeText(latest.type),
        content: latest.reason || '请遵守平台社区规范，文明交流。',
        confirmText: '查看详情',
        cancelText: '知道了',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/violation-notices/violation-notices' })
          }
        }
      })
      return count
    }).catch(() => count)
  })
}

module.exports = {
  typeText,
  formatTime,
  loadUnreadCount,
  checkAndPrompt
}
