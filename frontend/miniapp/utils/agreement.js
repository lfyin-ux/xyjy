const VERSION = '1.0'
const STORAGE_KEY = 'privacyAgreed'

function getAccepted() {
  const data = wx.getStorageSync(STORAGE_KEY)
  return data && data.version === VERSION ? data : null
}

function hasAccepted() {
  return !!getAccepted()
}

function accept() {
  wx.setStorageSync(STORAGE_KEY, {
    version: VERSION,
    time: Date.now()
  })
}

function requireAccepted(page) {
  if (hasAccepted()) return true
  wx.showToast({ title: '请先阅读并同意相关协议', icon: 'none' })
  return false
}

module.exports = {
  VERSION,
  getAccepted,
  hasAccepted,
  accept,
  requireAccepted
}
