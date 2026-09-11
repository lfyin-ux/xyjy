// 网络请求封装
const app = getApp()

function getBaseUrl() {
  return getApp().globalData.baseUrl
}

// 通用请求
function request(url, method, data) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: getBaseUrl() + url,
      method: method || 'GET',
      data: data || {},
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          resolve(res.data.data)
        } else {
          wx.showToast({ title: (res.data && res.data.msg) || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 文件上传 本地上传 返回可访问相对路径
function uploadFile(filePath, bizDir) {
  const userId = getApp().globalData.userId
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: getBaseUrl() + '/file/upload',
      filePath: filePath,
      name: 'file',
      formData: {
        bizDir: bizDir || 'common',
        userId: userId || ''
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data.url)
        } else {
          wx.showToast({ title: data.msg || '上传失败', icon: 'none' })
          reject(data)
        }
      },
      fail: reject
    })
  })
}

// 拼接图片完整地址 静态资源同样在context-path /api 下
function imgUrl(path) {
  if (!path) return ''
  if (path.indexOf('http') === 0) return path
  // 图片存储路径为 /uploads/xxx 实际访问需带上 /api 前缀
  return getBaseUrl() + path
}

module.exports = {
  get: (url, data) => request(url, 'GET', data),
  post: (url, data) => request(url, 'POST', data),
  del: (url, data) => request(url, 'DELETE', data),
  uploadFile,
  imgUrl
}
