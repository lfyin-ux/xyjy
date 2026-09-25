/** 将后端 LocalDateTime 格式化为可读时间 */
export function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}
