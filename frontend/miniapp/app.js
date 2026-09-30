const schoolContext = require('./utils/schoolContext')
const appConfig = require('./utils/appConfig')
const api = require('./utils/api')

// 小程序入口
App({
  globalData: {
    // 后端接口基础地址 真机调试请改为局域网IP
    baseUrl: 'https://tongxingshikong.cn/api',
    // 当前登录用户
    userInfo: null,
    // 当前用户ID
    userId: null,
    // 学校浏览上下文
    schoolContext: null,
    // 是否开启商城（由 /common/app-config 下发）
    mallEnabled: null
  },

  onLaunch() {
    const cached = wx.getStorageSync('appConfig')
    if (cached && cached.mallEnabled != null) {
      this.globalData.mallEnabled = !!cached.mallEnabled
    } else {
      this.globalData.mallEnabled = true
    }
    appConfig.load(this)

    const userInfo = wx.getStorageSync('userInfo')
    const userId = wx.getStorageSync('userId')
    if (userInfo && userId) {
      this.globalData.userInfo = userInfo
      this.globalData.userId = Number(userId)
      this.verifyAccountStatus(Number(userId))
      schoolContext.load(this)
    }
  },

  verifyAccountStatus(userId) {
    api.get('/user/detail/' + userId).then((user) => {
      if (user && user.status === 3) {
        this.clearLogin('账号已被封禁，无法登录')
      }
    }).catch(() => {})
  },

  clearLogin(message) {
    this.globalData.userInfo = null
    this.globalData.userId = null
    this.globalData.schoolContext = null
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('userId')
    wx.removeStorageSync('schoolContext')
    if (message) {
      wx.showToast({ title: message, icon: 'none', duration: 3000 })
    }
  },

  /**
   * 检查是否已登录 未登录跳转登录页
   */
  checkLogin() {
    if (!this.globalData.userId) {
      wx.navigateTo({ url: '/pages/login/login' })
      return false
    }
    return true
  }
})
