const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    mode: 'dev',
    users: [],
    currentId: null,
    currentUser: null,
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    // 已登录直接跳转主页
    if (app.globalData.userId) {
      wx.switchTab({ url: '/pages/match/match' })
      return
    }
    this.checkMode()
  },

  // 获取应用模式 决定显示哪种登录界面
  checkMode() {
    api.get('/auth/mode').then((res) => {
      this.setData({ mode: res.mode })
      if (res.mode === 'dev') {
        this.loadDevUsers()
      }
    }).catch(() => {
      // 接口失败默认开发模式
      this.setData({ mode: 'dev' })
      this.loadDevUsers()
    })
  },

  // 开发模式加载用户列表
  loadDevUsers() {
    api.get('/auth/devUsers').then((users) => {
      this.setData({ users })
    })
  },

  // 选择用户
  selectUser(e) {
    const item = e.currentTarget.dataset.item
    this.setData({ currentId: item.id, currentUser: item })
  },

  // 开发模式登录 直接用选中用户的openid
  devLogin() {
    if (!this.data.currentUser) return
    const user = this.data.currentUser
    api.post('/auth/wxLogin?openid=' + encodeURIComponent(user.openid)).then((loginUser) => {
      // 存储登录用户信息
      app.globalData.userInfo = loginUser
      app.globalData.userId = loginUser.id
      wx.setStorageSync('userInfo', loginUser)
      wx.setStorageSync('userId', loginUser.id)
      wx.showToast({ title: '已登录：' + loginUser.nickname, icon: 'none' })
      // 跳转到主页
      setTimeout(() => {
        wx.switchTab({ url: '/pages/match/match' })
      }, 500)
    })
  },

  // 生产模式微信授权登录
  wxLogin() {
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          wx.showToast({ title: '微信登录失败', icon: 'none' })
          return
        }
        // 将code发给后端换取openid并登录
        api.post('/auth/wxCodeLogin?code=' + loginRes.code).then((user) => {
          app.globalData.userInfo = user
          app.globalData.userId = user.id
          wx.setStorageSync('userInfo', user)
          wx.setStorageSync('userId', user.id)
          wx.showToast({ title: '登录成功', icon: 'success' })
          setTimeout(() => {
            wx.switchTab({ url: '/pages/match/match' })
          }, 500)
        })
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
      }
    })
  }
})
