const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    photos: [],
    imgBase: '',
    previewUrl: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.load()
  },

  load() {
    api.get('/user/photos/' + app.globalData.userId).then((list) => {
      this.setData({ photos: list })
    })
  },

  addPhoto() {
    wx.chooseMedia({
      count: 9 - this.data.photos.length,
      mediaType: ['image'],
      success: (res) => {
        wx.showLoading({ title: '上传中' })
        const tasks = res.tempFiles.map((f) => api.uploadFile(f.tempFilePath, 'photo'))
        Promise.all(tasks).then((urls) => {
          wx.hideLoading()
          // 逐张保存到相册
          const saves = urls.map((url, i) => api.post('/user/photo/add', {
            userId: app.globalData.userId,
            imgUrl: url,
            sort: this.data.photos.length + i
          }))
          Promise.all(saves).then(() => {
            wx.showToast({ title: '上传成功', icon: 'success' })
            this.load()
          })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  delPhoto(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定删除这张照片吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/user/photo/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' })
            this.load()
          })
        }
      }
    })
  },

  preview(e) {
    this.setData({ previewUrl: e.currentTarget.dataset.url })
  },

  closePreview() {
    this.setData({ previewUrl: '' })
  }
})
