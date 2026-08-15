const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    content: '',
    images: [],
    topic: '',
    place: '',
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onContent(e) { this.setData({ content: e.detail.value }) },
  onTopic(e) { this.setData({ topic: e.detail.value }) },
  onPlace(e) { this.setData({ place: e.detail.value }) },

  addImg() {
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      success: (res) => {
        wx.showLoading({ title: '上传中' })
        const tasks = res.tempFiles.map((f) => api.uploadFile(f.tempFilePath, 'post'))
        Promise.all(tasks).then((urls) => {
          wx.hideLoading()
          this.setData({ images: this.data.images.concat(urls) })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  delImg(e) {
    const idx = e.currentTarget.dataset.idx
    const images = this.data.images.slice()
    images.splice(idx, 1)
    this.setData({ images })
  },

  publish() {
    if (!this.data.content) {
      wx.showToast({ title: '请输入动态内容', icon: 'none' })
      return
    }
    api.post('/square/publish', {
      userId: app.globalData.userId,
      content: this.data.content,
      images: this.data.images.join(','),
      topic: this.data.topic,
      place: this.data.place
    }).then((res) => {
      wx.showToast({ title: res.tip || '发布成功', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 800)
    })
  }
})
