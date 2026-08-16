// 小程序入口
const ws = require('./utils/ws')

App({
  globalData: {
    // 后端接口基础地址 真机调试请改为局域网IP
    baseUrl: 'http://localhost:8080/api',
    // 当前登录用户
    userInfo: null,
    // 当前用户ID
    userId: null
  },

  onLaunch() {
    // 从本地缓存恢复登录状态
    const userInfo = wx.getStorageSync('userInfo')
    const userId = wx.getStorageSync('userId')
    if (userInfo && userId) {
      this.globalData.userInfo = userInfo
      this.globalData.userId = Number(userId)
      // 已登录 建立WebSocket连接并检查未读
      ws.connect()
      setTimeout(() => { this.updateChatBadge() }, 1500)
      // 开发模式定时检查角标（WebSocket对localhost不稳定时兜底）
      // 上线后WebSocket正常工作 此轮询自动不需要
      this.startBadgePolling()
    }
  },

  /**
   * 检查聊天未读数并设置tabBar角标
   */
  updateChatBadge() {
    const userId = this.globalData.userId
    if (!userId) return
    const api = require('./utils/api')
    api.get('/chat/sessions/' + userId).then((list) => {
      let total = 0
      list.forEach((s) => { total += (s.unread || 0) })
      if (total > 0) {
        wx.setTabBarBadge({ index: 3, text: String(total) })
      } else {
        wx.removeTabBarBadge({ index: 3 })
      }
    }).catch(() => {})
  },

  /**
   * 登录成功后调用 建立WebSocket连接
   */
  connectWs() {
    ws.connect()
    // 收到新消息时更新tabBar角标（WebSocket正常时秒级触发 无需轮询）
    ws.addListener(() => {
      this.updateChatBadge()
    })
    setTimeout(() => { this.updateChatBadge() }, 1000)
    this.startBadgePolling()
  },

  /**
   * 角标轮询 仅开发模式使用 上线后去掉
   * 上线时把下面的 interval 改成 0 或删掉整个方法即可
   */
  startBadgePolling() {
    if (this._badgeTimer) return
    // 开发模式5秒轮询 生产模式不轮询（靠WebSocket推送）
    var interval = 5000  // 上线后改为 0 关闭轮询
    if (interval > 0) {
      this._badgeTimer = setInterval(() => { this.updateChatBadge() }, interval)
    }
  },

  /**
   * 退出登录时调用 关闭WebSocket连接
   */
  disconnectWs() {
    ws.close()
    if (this._badgeTimer) {
      clearInterval(this._badgeTimer)
      this._badgeTimer = null
    }
  },

  /**
   * 检查是否已登录 未登录跳转登录页
   */
  checkLogin() {
    if (!this.globalData.userId) {
      wx.redirectTo({ url: '/pages/login/login' })
      return false
    }
    return true
  }
})
