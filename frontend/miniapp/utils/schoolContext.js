const api = require('./api')

function load(app) {
  const userId = app.globalData.userId
  if (!userId) {
    app.globalData.schoolContext = null
    wx.removeStorageSync('schoolContext')
    return Promise.resolve(null)
  }
  return api.get('/user/school/context?userId=' + userId).then((ctx) => {
    app.globalData.schoolContext = ctx
    wx.setStorageSync('schoolContext', ctx)
    return ctx
  }).catch(() => null)
}

function get(app) {
  return app.globalData.schoolContext || wx.getStorageSync('schoolContext') || null
}

function isViewOnly(app) {
  const ctx = get(app)
  return !!(ctx && ctx.viewOnly)
}

function canWrite(app) {
  const ctx = get(app)
  return !ctx || !!ctx.canWrite
}

function ensureWrite(app, actionLabel) {
  if (canWrite(app)) return true
  const ctx = get(app) || {}
  const spent = ctx.mallTotalSpent != null ? ctx.mallTotalSpent : 0
  const remain = ctx.unlockRemain != null ? ctx.unlockRemain : 2000
  wx.showModal({
    title: '当前为浏览模式',
    content: `你正在浏览「${ctx.currentSchoolName || '其他学校'}」，暂不可${actionLabel}。在本校商城累计消费满2000元后可在外校互动（已消费¥${spent}，还差¥${remain}）。`,
    confirmText: '去商城',
    cancelText: '知道了',
    success: (res) => {
      if (res.confirm) wx.switchTab({ url: '/pages/mall/mall' })
    }
  })
  return false
}

module.exports = { load, get, isViewOnly, canWrite, ensureWrite }
