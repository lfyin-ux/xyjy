const api = require('../../utils/api')
const authGate = require('../../utils/authGate')
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
    if (this.getTabBar()) this.getTabBar().setData({ selected: 1 })
    this.checkAuth()
  },

  checkAuth() {
    const userId = app.globalData.userId
    // 校园生活的公开列表支持游客浏览。
    if (!userId) {
      this.setData({ locked: false })
      this.loadGoods()
      this.loadTasks()
      return
    }
    authGate.checkFullAccess(userId, () => {
      this.setData({ locked: false })
      this.loadGoods()
      this.loadTasks()
    }, () => {
      this.setData({ locked: true })
    })
    /* TODO: 审核通过后恢复双认证门禁（并将 authGate.js 中 AUTH_GATE_ENABLED 改为 true）
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
    */
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
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/auth/auth' })
  }
})
