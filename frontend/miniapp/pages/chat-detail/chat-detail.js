const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    sessionId: null,
    otherId: null,
    myId: 0,
    messages: [],
    text: '',
    locked: false,
    lastId: ''
  },

  onLoad(options) {
    this.setData({
      sessionId: options.sessionId,
      otherId: options.otherId,
      myId: app.globalData.userId
    })
    if (options.name) {
      wx.setNavigationBarTitle({ title: options.name })
    }
    this.loadMessages()
  },

  loadMessages() {
    api.get('/chat/messages/' + this.data.sessionId + '?userId=' + app.globalData.userId).then((list) => {
      this.setData({
        messages: list,
        lastId: list.length ? list[list.length - 1].id : ''
      })
    })
  },

  onInput(e) {
    this.setData({ text: e.detail.value })
  },

  send() {
    const text = this.data.text.trim()
    if (!text) {
      wx.showToast({ title: '请输入消息', icon: 'none' })
      return
    }
    api.post('/chat/send', {
      sessionId: Number(this.data.sessionId),
      fromId: app.globalData.userId,
      content: text,
      msgType: 1
    }).then(() => {
      this.setData({ text: '' })
      this.loadMessages()
    })
  }
})
