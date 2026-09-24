/** JS 层展示格式化，与 utils/display.wxs 逻辑保持一致 */

function displayText(value, fallback = '') {
  if (value == null || value === '') return fallback
  const s = String(value).trim()
  if (!s || s === 'null' || s === 'undefined') return fallback
  return s
}

function joinText(parts, sep = ' · ', fallback = '') {
  const list = (parts || []).map((p) => displayText(p)).filter(Boolean)
  return list.length ? list.join(sep) : fallback
}

function formatPostSub(school, place) {
  return joinText([school, place], ' · ', '同校动态')
}

function formatProfileSchool(school, identityVerified) {
  const schoolText = displayText(school)
  const verified = identityVerified === 1
  if (schoolText && verified) return joinText([schoolText, '已实名 ✓'])
  if (schoolText && !verified) return joinText([schoolText, '待实名认证'])
  if (!schoolText && verified) return '学校待认证 · 已实名 ✓'
  return '待完成认证'
}

function formatMineAuth(school, authPassed) {
  if (!authPassed) return '账号已注册 · 待完成认证'
  const schoolText = displayText(school)
  return schoolText ? `${schoolText} · 双认证已通过 ✓` : '双认证已通过 ✓'
}

function formatUserSub(school, college) {
  return joinText([school, college], ' · ', '校园用户')
}

module.exports = {
  displayText,
  joinText,
  formatPostSub,
  formatProfileSchool,
  formatMineAuth,
  formatUserSub
}
