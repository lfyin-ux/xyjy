const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    name: '',
    category: '',
    conditionDesc: '',
    price: '',
    description: '',
    images: [],
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onName(e) { this.setData({ name: e.detail.value }) },
  onCategory(e) { this.setData({ category: e.detail.value }) },
  onCondition(e) { this.setData({ conditionDesc: e.detail.value }) },
  onPrice(e) { this.setData({ price: e.detail.value }) },
  onDesc(e) { this.setData({ description: e.detail.value }) },

  addImg() {
    wx.chooseMedia({
      count: 6 - this.data.images.length,
      mediaType: ['image'],
      success: (res) => {
        wx.showLoading({ title: '上传中' })
        const tasks = res.tempFiles.map((f) => api.uploadFile(f.tempFilePath, 'second'))
        Promise.all(tasks).then((urls) => {
          wx.hideLoading()
          this.setData({ images: this.data.images.concat(urls) })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  delImg(e) {
    const images = this.data.images.slice()
    images.splice(e.currentTarget.dataset.idx, 1)
    this.setData({ images })
  },

  publish() {
    const d = this.data
    if (!d.name || !d.price) {
      wx.showToast({ title: '请填写商品名称和价格', icon: 'none' })
      return
    }
    api.post('/second/publish', {
      sellerId: app.globalData.userId,
      name: d.name,
      category: d.category,
      conditionDesc: d.conditionDesc,
      price: Number(d.price),
      description: d.description,
      images: d.images.join(',')
    }).then((tip) => {
      wx.showToast({ title: tip || '发布成功', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 800)
    })
  }
})
