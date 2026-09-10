const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    goods: [],
    tasks: [],
    imgBase: '',
    locked: false
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.checkAuth()
  },

  checkAuth() {
    const userId = app.globalData.userId
    if (!userId) {
      this.setData({ locked: true })
      return
    }
    api.get('/auth/status/' + userId).then((res) => {
      if (res.fullAccess) {
        this.setData({ locked: false })
        this.loadGoods()
        this.loadTasks()
      } else {
        this.setData({ locked: true })
      }
    }).catch(() => {
      this.setData({ locked: true })
    })
  },

  // 二手商品预览
  loadGoods() {
    api.get('/second/list').then((list) => {
      const goods = list.slice(0, 3).map((g) => {
        g.firstImg = g.images ? g.images.split(',')[0] : ''
        return g
      })
      this.setData({ goods })
    })
  },

  // 待接跑腿预览
  loadTasks() {
    api.get('/errand/available').then((list) => {
      this.setData({ tasks: list.slice(0, 3) })
    })
  },

  goErrand() {
    wx.navigateTo({ url: '/pages/errand/errand' })
  },

  goSecond() {
    wx.navigateTo({ url: '/pages/second/second' })
  },

  goGame() {
    wx.navigateTo({ url: '/pages/game/game' })
  },

  noop() {
    wx.showToast({ title: '更多服务即将开放', icon: 'none' })
  },

  goAuth() {
    wx.navigateTo({ url: '/pages/auth/auth' })
  }
})
