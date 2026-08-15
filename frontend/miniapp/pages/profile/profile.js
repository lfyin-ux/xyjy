const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    mode: 'detail',
    user: {},
    tagList: [],
    list: [],
    imgBase: ''
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.list) {
      // 列表模式
      this.setData({ mode: 'list' })
      if (options.list === 'likeMe') {
        wx.setNavigationBarTitle({ title: '谁喜欢我' })
        api.get('/match/whoLikesMe/' + app.globalData.userId).then((list) => this.setData({ list }))
      } else {
        wx.setNavigationBarTitle({ title: '我的访客' })
        api.get('/user/visitors/' + app.globalData.userId).then((visits) => {
          // 访客记录只有id 需要拉取用户信息 这里简单展示访客ID对应用户
          const ids = [...new Set(visits.map((v) => v.userId))]
          Promise.all(ids.map((id) => api.get('/user/detail/' + id))).then((users) => {
            this.setData({ list: users })
          })
        })
      }
    } else if (options.id) {
      this.setData({ mode: 'detail', targetId: options.id })
      this.loadDetail(options.id)
    }
  },

  loadDetail(id) {
    api.get('/user/detail/' + id + '?visitorId=' + app.globalData.userId).then((user) => {
      this.setData({
        user,
        tagList: user.tags ? user.tags.split(',') : []
      })
    })
  },

  viewUser(e) {
    wx.navigateTo({ url: '/pages/profile/profile?id=' + e.currentTarget.dataset.id })
  },

  follow() {
    wx.showToast({ title: '已关注 TA', icon: 'none' })
  },

  sayHi() {
    api.post('/chat/hello?fromId=' + app.globalData.userId + '&toId=' + this.data.targetId + '&content=' + encodeURIComponent('你好，很高兴认识你 👋')).then((session) => {
      wx.showToast({ title: '招呼已发送', icon: 'success' })
      setTimeout(() => {
        wx.navigateTo({ url: '/pages/chat-detail/chat-detail?sessionId=' + session.id + '&otherId=' + this.data.targetId + '&name=' + this.data.user.nickname })
      }, 600)
    })
  }
})
