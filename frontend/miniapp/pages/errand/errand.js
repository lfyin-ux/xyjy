const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    mode: 'take',
    tasks: [],
    myPublish: [],
    myTaken: [],
    imgBase: ''
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.my) {
      if (!app.checkLogin()) return
      this.setData({ mode: 'publish' })
    }
  },

  onShow() {
    this.loadAll()
  },

  switchMode(e) {
    this.setData({ mode: e.currentTarget.dataset.m })
  },

  loadAll() {
    this.loadTasks()
    if (!app.globalData.userId) return
    this.loadMyPublish()
    this.loadMyTaken()
  },

  // 可接单列表
  loadTasks() {
    api.get('/errand/available').then((list) => this.setData({ tasks: list }))
  },

  // 我发布的
  loadMyPublish() {
    api.get('/errand/my/publish/' + app.globalData.userId).then((list) => {
      // 加载接单人信息
      const tasks = list.map((item) => {
        item.takerInfo = null
        if (item.takerId) {
          api.get('/user/detail/' + item.takerId).then((user) => {
            item.takerInfo = user
            this.setData({ myPublish: this.data.myPublish })
          })
        }
        return item
      })
      this.setData({ myPublish: tasks })
    })
  },

  // 我接取的
  loadMyTaken() {
    api.get('/errand/my/take/' + app.globalData.userId).then((list) => this.setData({ myTaken: list }))
  },

  // 接单 跳转到确认接单页
  accept(e) {
    if (!app.checkLogin()) return
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/errand-accept/errand-accept?id=' + id })
  },

  // 发单人确认完成
  finish(e) {
    const id = e.currentTarget.dataset.id
    api.post('/errand/updateStatus?id=' + id + '&status=3').then(() => {
      wx.showToast({ title: '已确认完成', icon: 'success' })
      this.loadAll()
    })
  },

  // 接单人标记完成
  finishTaken(e) {
    const id = e.currentTarget.dataset.id
    api.post('/errand/updateStatus?id=' + id + '&status=3').then(() => {
      wx.showToast({ title: '已标记完成', icon: 'success' })
      this.loadAll()
    })
  },

  // 取消订单
  cancel(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          api.post('/errand/updateStatus?id=' + id + '&status=4').then(() => {
            wx.showToast({ title: '已取消', icon: 'none' })
            this.loadAll()
          })
        }
      }
    })
  },

  goPublish() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/errand-publish/errand-publish' })
  },

  viewTaker(e) {
    const id = e.currentTarget.dataset.id
    if (id) {
      wx.navigateTo({ url: '/pages/profile/profile?id=' + id })
    }
  }
})
