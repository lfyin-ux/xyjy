const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [],
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.load()
  },

  // 加载黑名单 并拉取被拉黑用户信息
  load() {
    api.get('/user/blacklist/' + app.globalData.userId).then((records) => {
      if (!records.length) {
        this.setData({ list: [] })
        return
      }
      const tasks = records.map((b) =>
        api.get('/user/detail/' + b.targetId).then((user) => ({ blacklist: b, user }))
      )
      Promise.all(tasks).then((list) => this.setData({ list }))
    })
  },

  remove(e) {
    const targetId = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定将该用户移出黑名单吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/user/blacklist?userId=' + app.globalData.userId + '&targetId=' + targetId).then(() => {
            wx.showToast({ title: '已移出', icon: 'success' })
            this.load()
          })
        }
      }
    })
  }
})
