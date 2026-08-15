const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    sessions: [],
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.loadSessions()
  },

  loadSessions() {
    api.get('/chat/sessions/' + app.globalData.userId).then((list) => {
      const sessions = list.map((s) => {
        s.timeText = this.formatTime(s.session.lastTime)
        return s
      })
      this.setData({ sessions })
    })
  },

  formatTime(t) {
    if (!t) return ''
    // 只取时分或日期
    const str = t.replace('T', ' ')
    return str.substring(5, 16)
  },

  openChat(e) {
    const item = e.currentTarget.dataset.item
    const otherId = item.other.id
    const sessionId = item.session.id
    wx.navigateTo({
      url: '/pages/chat-detail/chat-detail?sessionId=' + sessionId + '&otherId=' + otherId + '&name=' + item.other.nickname
    })
  },

  noop() {
    wx.showToast({ title: '消息搜索已打开', icon: 'none' })
  }
})
