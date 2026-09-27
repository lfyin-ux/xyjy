const api = require('./api')

function firstImage(images) {
  if (!images) return ''
  return String(images).split(',')[0].trim()
}

function imageUrls(images) {
  if (!images) return []
  return String(images)
    .split(',')
    .map((s) => api.imgUrl(s.trim()))
    .filter(Boolean)
}

function coverUrl(images) {
  return api.imgUrl(firstImage(images))
}

module.exports = {
  firstImage,
  imageUrls,
  coverUrl
}
