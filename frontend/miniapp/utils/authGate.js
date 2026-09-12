/**
 * 双认证门禁开关（与后端 AuthCheckService 同步）
 * TODO: 小程序审核通过后，将 AUTH_GATE_ENABLED 改为 true 并恢复各页面 checkAuth 中的注释逻辑
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

module.exports = {
  AUTH_GATE_ENABLED,
  checkFullAccess
}
