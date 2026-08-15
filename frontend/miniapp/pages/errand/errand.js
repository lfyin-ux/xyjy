const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    mode: 'take',
    tasks: [],
    myTasks: []
  },

  onLoad(options) {
    if (options.my) {
      this.setData({ mode: 'mine' })
    }
  },

  onShow() {
    this.loadTasks()
    this.loadMyTasks()
  },

  switchMode(e) {
    this.setData({ mode: e.currentTarget.dataset.m })
  },

  loadTasks() {
    api.get('/errand/available').then((list) => this.setData({ tasks: list }))
  },

  loadMyTasks() {
    api.get('/errand/my/publish/' + app.globalData.userId).then((list) => this.setData({ myTasks: list }))
  },

  accept(e) {
    const id = e.currentTarget.dataset.id
    api.post('/errand/accept?id=' + id + '&takerId=' + app.globalData.userId).then(() => {
      wx.showToast({ title: '接单成功', icon: 'success' })
      this.loadTasks()
    })
  },

  cancel(e) {
    const id = e.currentTarget.dataset.id
    api.post('/errand/updateStatus?id=' + id + '&status=4').then(() => {
      wx.showToast({ title: '已取消', icon: 'none' })
      this.loadMyTasks()
    })
  },

  finish(e) {
    const id = e.currentTarget.dataset.id
    api.post('/errand/updateStatus?id=' + id + '&status=3').then(() => {
      wx.showToast({ title: '已完成', icon: 'success' })
      this.loadMyTasks()
    })
  },

  goPublish() {
    wx.navigateTo({ url: '/pages/errand-publish/errand-publish' })
  }
})
