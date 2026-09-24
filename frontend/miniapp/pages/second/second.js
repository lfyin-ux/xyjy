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
      const uid = app.globalData.userId
      let url = '/second/list?'
      const parts = []
      if (uid) parts.push('userId=' + uid)
      if (this.data.keyword) parts.push('keyword=' + encodeURIComponent(this.data.keyword))
      url += parts.join('&')
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

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/second-detail/second-detail?id=' + id })
  },

  goPublish() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/second-publish/second-publish' })
  }
})
