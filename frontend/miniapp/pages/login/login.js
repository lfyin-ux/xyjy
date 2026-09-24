const api = require('../../utils/api')
const agreement = require('../../utils/agreement')
const app = getApp()

Page({
  data: {
    mode: 'dev',
    users: [],
    currentId: null,
    currentUser: null,
    imgBase: '',
    agreed: false
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (app.globalData.userId) {
      wx.switchTab({ url: '/pages/match/match' })
      return
    }
    if (agreement.hasAccepted()) {
      this.setData({ agreed: true })
    }
    this.checkMode()
  },

  checkMode() {
    api.get('/auth/mode').then((res) => {
      this.setData({ mode: res.mode })
      if (res.mode === 'dev') {
        this.loadDevUsers()
      }
    }).catch(() => {
      this.setData({ mode: 'dev' })
      this.loadDevUsers()
    })
  },

  loadDevUsers() {
    api.get('/auth/devUsers').then((users) => {
      this.setData({ users })
    })
  },

  selectUser(e) {
    const item = e.currentTarget.dataset.item
    this.setData({ currentId: item.id, currentUser: item })
  },

  toggleAgree() {
    const agreed = !this.data.agreed
    this.setData({ agreed })
    if (agreed) {
      agreement.accept()
    }
  },

  openAgreement(e) {
    const type = e.currentTarget.dataset.type
    const url = type === 'privacy'
      ? '/pages/privacy-policy/privacy-policy'
      : '/pages/user-agreement/user-agreement'
    wx.navigateTo({ url })
  },

  goBrowse() {
    wx.switchTab({ url: '/pages/match/match' })
  },

  devLogin() {
    if (!agreement.requireAccepted(this)) return
    if (!this.data.currentUser) return
    const user = this.data.currentUser
    api.post('/auth/wxLogin?openid=' + encodeURIComponent(user.openid)).then((loginUser) => {
      if (loginUser.status === 3) {
        wx.showToast({ title: '账号已被封禁，无法登录', icon: 'none' })
        return
      }
      app.globalData.userInfo = loginUser
      app.globalData.userId = loginUser.id
      wx.setStorageSync('userInfo', loginUser)
      wx.setStorageSync('userId', loginUser.id)
      wx.showToast({ title: '已登录：' + loginUser.nickname, icon: 'none' })
      setTimeout(() => {
        wx.switchTab({ url: '/pages/match/match' })
      }, 500)
    })
  },

  wxLogin() {
    if (!agreement.requireAccepted(this)) return
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          wx.showToast({ title: '微信登录失败', icon: 'none' })
          return
        }
        api.post('/auth/wxCodeLogin?code=' + loginRes.code).then((user) => {
          if (user.status === 3) {
            wx.showToast({ title: '账号已被封禁，无法登录', icon: 'none' })
            return
          }
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
