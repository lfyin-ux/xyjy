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
  let mallHint = ''
  try {
    const appConfig = require('./appConfig')
    if (appConfig.isMallEnabled(app)) {
      mallHint = `在本校商城累计消费满2000元后可在外校互动（已消费¥${spent}，还差¥${remain}）。`
    }
  } catch (e) {
    mallHint = `在本校商城累计消费满2000元后可在外校互动（已消费¥${spent}，还差¥${remain}）。`
  }
  const content = mallHint
    ? `你正在浏览「${ctx.currentSchoolName || '其他学校'}」，暂不可${actionLabel}。${mallHint}`
    : `你正在浏览「${ctx.currentSchoolName || '其他学校'}」，暂不可${actionLabel}。`
  const modal = {
    title: '当前为浏览模式',
    content,
    cancelText: '知道了',
    success: () => {}
  }
  try {
    const appConfig = require('./appConfig')
    if (appConfig.isMallEnabled(app)) {
      modal.confirmText = '去商城'
      modal.success = (res) => {
        if (res.confirm) wx.switchTab({ url: '/pages/mall/mall' })
      }
    } else {
      modal.showCancel = false
    }
  } catch (e) {
    modal.confirmText = '去商城'
    modal.success = (res) => {
      if (res.confirm) wx.switchTab({ url: '/pages/mall/mall' })
    }
  }
  wx.showModal(modal)
  return false
}

module.exports = { load, get, isViewOnly, canWrite, ensureWrite }
