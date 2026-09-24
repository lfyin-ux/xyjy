const api = require('../../utils/api')
const ws = require('../../utils/ws')
const app = getApp()

Page({
  data: {
    sessionId: null,
    otherId: null,
    myId: 0,
    messages: [],
    text: '',
    locked: false,
    lastId: '',
    myAvatar: '',
    otherAvatar: '',
    imgBase: '',
    showEmoji: false,
    emojis: ['😀','😂','🥰','😍','😘','🤗','😎','🤔','😅','😢','😡','👍','👏','🎉','❤️','💜','🔥','⭐','🌈','🎮','⚽','🏀','📷','🎵','☕','🍰','🌸','💪','✌️','🙏'],
    pageNum: 1,
    hasMore: false,
    loadingMore: false
  },

  // WebSocket监听移除函数
  _removeListener: null,

  onLoad(options) {
    const imgBase = app.globalData.baseUrl
    this.setData({
      sessionId: options.sessionId,
      otherId: options.otherId,
      myId: Number(app.globalData.userId),
      imgBase: imgBase
    })
    if (options.name) {
      wx.setNavigationBarTitle({ title: options.name })
    }
    // 加载自己的最新头像
    api.get('/user/detail/' + app.globalData.userId).then((me) => {
      this.setData({ myAvatar: me.avatar ? imgBase + me.avatar : '' })
    })
    // 加载对方头像
    api.get('/user/detail/' + options.otherId + '?visitorId=' + app.globalData.userId).then((user) => {
      this.setData({ otherAvatar: user.avatar ? imgBase + user.avatar : '' })
    })
    // 加载历史消息
    this.loadMessages()
    // 注册WebSocket监听 实时接收新消息
    this._removeListener = ws.addListener((msg) => {
      this.onWsMessage(msg)
    })
    // 确保WebSocket已连接
    ws.connect()
  },

  onUnload() {
    if (this._removeListener) {
      this._removeListener()
      this._removeListener = null
    }
    // 离开聊天页时标记已读
    if (this.data.sessionId) {
      api.get('/chat/messages/' + this.data.sessionId + '?userId=' + app.globalData.userId)
    }
  },

  // 收到WebSocket推送的新消息
  onWsMessage(msg) {
    if (String(msg.sessionId) === String(this.data.sessionId)) {
      msg.fromId = Number(msg.fromId)
      msg.msgType = Number(msg.msgType || 1)
      if (msg.fromId === this.data.myId) return
      // 去重
      if (msg.id && this.data.messages.some((m) => m.id === msg.id)) return
      const messages = this.data.messages.concat([msg])
      this.setData({
        messages: messages,
        lastId: 'msg-' + (msg.id || messages.length)
      })
      // 实时收到的消息立即标记已读（用户正在看这个会话）
      api.get('/chat/messages/' + this.data.sessionId + '?userId=' + app.globalData.userId)
    }
  },

  loadMessages() {
    api.get('/chat/messages/' + this.data.sessionId + '?userId=' + app.globalData.userId + '&pageNum=1&pageSize=20').then((res) => {
      var list = res.records || []
      list.forEach((m) => {
        m.fromId = Number(m.fromId)
        m.msgType = Number(m.msgType)
      })
      this.setData({
        messages: list,
        pageNum: 1,
        hasMore: res.hasMore,
        lastId: list.length ? 'msg-' + list[list.length - 1].id : ''
      })
    })
  },

  // 下拉加载更早的历史消息
  loadMore() {
    if (!this.data.hasMore || this.data.loadingMore) return
    this.setData({ loadingMore: true })
    var nextPage = this.data.pageNum + 1
    api.get('/chat/messages/' + this.data.sessionId + '?userId=' + app.globalData.userId + '&pageNum=' + nextPage + '&pageSize=20').then((res) => {
      var list = res.records || []
      list.forEach((m) => {
        m.fromId = Number(m.fromId)
        m.msgType = Number(m.msgType)
      })
      // 历史消息插入到前面
      var messages = list.concat(this.data.messages)
      this.setData({
        messages: messages,
        pageNum: nextPage,
        hasMore: res.hasMore,
        loadingMore: false
      })
    }).catch(() => {
      this.setData({ loadingMore: false })
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
    this.setData({ showEmoji: false })
    api.post('/chat/send', {
      sessionId: Number(this.data.sessionId),
      fromId: app.globalData.userId,
      content: text,
      msgType: 1
    }).then((msg) => {
      msg.fromId = Number(msg.fromId)
      msg.msgType = Number(msg.msgType)
      const messages = this.data.messages.concat([msg])
      this.setData({
        text: '',
        messages: messages,
        lastId: 'msg-' + (msg.id || messages.length)
      })
    })
  },

  // 切换emoji面板
  toggleEmoji() {
    this.setData({ showEmoji: !this.data.showEmoji })
  },

  // 点击emoji插入到输入框
  insertEmoji(e) {
    this.setData({ text: this.data.text + e.currentTarget.dataset.e })
  },

  // 发送图片消息
  sendImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const path = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '发送中' })
        api.uploadFile(path, 'chat').then((url) => {
          wx.hideLoading()
          // 发送图片消息 content存图片路径 msgType=3
          api.post('/chat/send', {
            sessionId: Number(this.data.sessionId),
            fromId: app.globalData.userId,
            content: url,
            msgType: 3
          }).then((msg) => {
            msg.fromId = Number(msg.fromId)
            msg.msgType = Number(msg.msgType)
            const messages = this.data.messages.concat([msg])
            this.setData({
              messages: messages,
              lastId: 'msg-' + (msg.id || messages.length)
            })
          })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  // 预览图片消息
  previewImg(e) {
    wx.previewImage({
      current: e.currentTarget.dataset.url,
      urls: [e.currentTarget.dataset.url]
    })
  }
})
