const api = require('../../utils/api')
const ws = require('../../utils/ws')
const app = getApp()

Page({
  data: {
    sessions: [],
    imgBase: '',
    locked: false
  },

  // WebSocket监听移除函数
  _removeListener: null,

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
        this.loadSessions()
      } else {
        this.setData({ locked: true })
      }
    }).catch(() => {
      this.setData({ locked: true })
    })
  },

  onUnload() {
    if (this._removeListener) {
      this._removeListener()
      this._removeListener = null
    }
  },

  loadSessions() {
    api.get('/chat/sessions/' + app.globalData.userId).then((list) => {
      const sessions = list.map((s) => {
        s.timeText = this.formatTime(s.session.lastTime)
        // 图片消息显示[图片]
        var msg = s.session.lastMsg
        if (msg && msg.indexOf('/uploads/') === 0) {
          s.session.displayMsg = '[图片]'
        } else {
          s.session.displayMsg = msg || '暂无消息'
        }
        return s
      })
      this.setData({ sessions: sessions })
      app.updateChatBadge()
    })
  },

  formatTime(t) {
    if (!t) return ''
    // 兼容 2026-08-16T15:30:22 和 2026-08-16 15:30:22 格式
    var str = t.replace('T', ' ')
    var datePart = str.substring(0, 10)
    var timePart = str.substring(11, 16)
    // 判断是否今天
    var now = new Date()
    var y = now.getFullYear()
    var m = ('0' + (now.getMonth() + 1)).slice(-2)
    var d = ('0' + now.getDate()).slice(-2)
    var today = y + '-' + m + '-' + d
    if (datePart === today) {
      return timePart
    }
    // 非今天 显示月-日 时:分
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

  // 左滑删除相关
  touchStart(e) {
    this._touchStartX = e.touches[0].clientX
    this._touchIdx = e.currentTarget.dataset.idx
  },

  touchMove(e) {
    const dx = e.touches[0].clientX - this._touchStartX
    const idx = this._touchIdx
    if (dx < -30) {
      // 左滑 显示删除按钮
      this.setData({ ['sessions[' + idx + '].offset']: -140 })
    } else if (dx > 30) {
      // 右滑 收回
      this.setData({ ['sessions[' + idx + '].offset']: 0 })
    }
  },

  touchEnd(e) {
    // 不做额外处理 保持当前状态
  },

  deleteSession(e) {
    const id = e.currentTarget.dataset.id
    const idx = e.currentTarget.dataset.idx
    wx.showModal({
      title: '提示',
      content: '确定删除该聊天会话吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/chat/session/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' })
            this.loadSessions()
          })
        } else {
          this.setData({ ['sessions[' + idx + '].offset']: 0 })
        }
      }
    })
  },

  noop() {
    wx.showToast({ title: '消息搜索已打开', icon: 'none' })
  },

  goAuth() {
    wx.navigateTo({ url: '/pages/auth/auth' })
  }
})
