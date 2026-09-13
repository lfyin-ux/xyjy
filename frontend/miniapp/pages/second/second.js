const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    mode: 'list',
    list: [],
    keyword: '',
    imgBase: ''
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.my) {
      if (!app.checkLogin()) return
      this.setData({ mode: 'my' })
      wx.setNavigationBarTitle({ title: '我的二手商品' })
    }
  },

  onShow() {
    this.load()
  },

  load() {
    if (this.data.mode === 'my') {
      api.get('/second/my/' + app.globalData.userId).then((list) => this.fill(list))
    } else {
      let url = '/second/list'
      if (this.data.keyword) url += '?keyword=' + this.data.keyword
      api.get(url).then((list) => this.fill(list))
    }
  },

  fill(list) {
    list.forEach((g) => {
      g.firstImg = g.images ? g.images.split(',')[0] : ''
    })
    this.setData({ list })
  },

  onInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  search() {
    this.load()
  },

  contact(e) {
    const item = e.currentTarget.dataset.item
    if (this.data.mode === 'my') return
    wx.showModal({
      title: item.name,
      content: item.description + '\n价格：¥' + item.price,
      confirmText: '联系卖家',
      success: (res) => {
        if (res.confirm) {
          if (!app.checkLogin()) return
          wx.navigateTo({ url: '/pages/profile/profile?id=' + item.sellerId })
        }
      }
    })
  },

  goPublish() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/second-publish/second-publish' })
  }
})
