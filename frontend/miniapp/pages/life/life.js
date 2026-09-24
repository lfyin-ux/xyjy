const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    goods: [],
    tasks: [],
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setData({ selected: 1 })
    this.loadGoods()
    this.loadTasks()
  },

  // 二手商品预览
  loadGoods() {
    const uid = app.globalData.userId
    const url = '/second/list' + (uid ? '?userId=' + uid : '')
    api.get(url).then((list) => {
      const goods = list.slice(0, 3).map((g) => {
        g.firstImg = g.images ? g.images.split(',')[0] : ''
        return g
      })
      this.setData({ goods })
    })
  },

  // 待接跑腿预览
  loadTasks() {
    const uid = app.globalData.userId
    const url = '/errand/available' + (uid ? '?userId=' + uid : '')
    api.get(url).then((list) => {
      this.setData({ tasks: list.slice(0, 3) })
    })
  },

  goErrand() {
    wx.navigateTo({ url: '/pages/errand/errand' })
  },

  goSecond() {
    wx.navigateTo({ url: '/pages/second/second' })
  },

  goSecondDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/second-detail/second-detail?id=' + id })
  },

  goGame() {
    wx.navigateTo({ url: '/pages/game/game' })
  },

  noop() {
    wx.showToast({ title: '更多服务即将开放', icon: 'none' })
  },

})
