const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    goods: {},
    imageList: [],
    imgBase: ''
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.id) {
      this.loadDetail(options.id)
    }
  },

  loadDetail(id) {
    const uid = app.globalData.userId
    const url = '/second/detail/' + id + (uid ? '?userId=' + uid : '')
    api.get(url).then((goods) => {
      const imageList = []
      if (goods.images) {
        goods.images.split(',').forEach((url) => {
          const u = (url || '').trim()
          if (u) imageList.push(u)
        })
      }
      this.setData({ goods, imageList })
    })
  },

  previewImg(e) {
    const idx = e.currentTarget.dataset.idx
    const urls = this.data.imageList.map((u) => this.data.imgBase + u)
    wx.previewImage({ current: urls[idx], urls })
  },

  copyContact() {
    if (!app.checkLogin()) return
    const contact = (this.data.goods.sellerContact || '').trim()
    if (!contact) {
      wx.showToast({ title: '卖家未留联系方式', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: contact,
      success: () => wx.showToast({ title: '已复制联系方式', icon: 'success' })
    })
  }
})
