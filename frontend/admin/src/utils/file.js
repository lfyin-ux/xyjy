/** 将后端返回的 /uploads/... 转为管理后台可访问路径 */
export function fileUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/api/')) return path
  if (path.startsWith('/uploads/')) return '/api' + path
  return path
}

export function splitFileUrls(value) {
  if (!value) return []
  return value.split(',').filter(Boolean).map(fileUrl)
}

export function firstFileUrl(value) {
  const list = splitFileUrls(value)
  return list[0] || ''
}
