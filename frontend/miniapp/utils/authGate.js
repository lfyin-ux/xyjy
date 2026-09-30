/**
 * 双认证门禁（广场互动等需 fullAccess；发布动态另看 canPublishPost）
 */
const AUTH_GATE_ENABLED = false

const api = require('./api')

function checkFullAccess(userId, onGranted, onDenied) {
  if (!AUTH_GATE_ENABLED) {
    if (userId) onGranted()
    else onDenied()
    return
  }
  if (!userId) {
    onDenied()
    return
  }
  api.get('/auth/status/' + userId).then((res) => {
    if (res.fullAccess) onGranted()
    else onDenied()
  }).catch(() => onDenied())
}

function checkCanPublish(userId, onGranted, onDenied) {
  if (!userId) {
    onDenied()
    return
  }
  api.get('/auth/status/' + userId).then((res) => {
    if (res.canPublishPost) onGranted()
    else onDenied()
  }).catch(() => onDenied())
}

module.exports = {
  AUTH_GATE_ENABLED,
  checkFullAccess,
  checkCanPublish
}
