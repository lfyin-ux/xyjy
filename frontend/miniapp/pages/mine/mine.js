const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    user: {},
    fullAccess: false,
    authSummary: '个人认证和学校认证均待完成',
    genderText: '',
    likeMeCount: 0,
    matchCount: 0,
    visitorCount: 0,
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.loadUser()
    this.loadStats()
  },

  loadUser() {
    const uid = app.globalData.userId
    api.get('/user/detail/' + uid).then((user) => {
      this.setData({
        user,
        genderText: user.gender === 1 ? '♂' : user.gender === 2 ? '♀' : ''
      })
    })
    // 认证状态
    api.get('/auth/status/' + uid).then((res) => {
      const statusText = (s) => (s === 2 ? '已通过' : s === 1 ? '审核中' : s === 3 ? '未通过' : '待认证')
      this.setData({
        fullAccess: res.fullAccess,
        authSummary: res.fullAccess ? '个人认证和学校认证均已通过'
          : '个人认证：' + statusText(res.personalStatus) + ' · 学校认证：' + statusText(res.schoolStatus)
      })
    })
  },

  loadStats() {
    const uid = app.globalData.userId
    api.get('/match/whoLikesMe/' + uid).then((list) => this.setData({ likeMeCount: list.length }))
    api.get('/match/list/' + uid).then((list) => this.setData({ matchCount: list.length }))
    api.get('/user/visitors/' + uid).then((list) => this.setData({ visitorCount: list.length }))
  },

  goAuth() {
    wx.navigateTo({ url: '/pages/auth/auth' })
  },

  goEditProfile() {
    wx.navigateTo({ url: '/pages/edit-profile/edit-profile' })
  },

  goChat() {
    wx.switchTab({ url: '/pages/chat/chat' })
  },

  goWhoLikesMe() {
    wx.navigateTo({ url: '/pages/profile/profile?list=likeMe' })
  },

  goVisitors() {
    wx.navigateTo({ url: '/pages/profile/profile?list=visitors' })
  },

  goMyPosts() {
    wx.navigateTo({ url: '/pages/post-detail/post-detail?my=1' })
  },

  goMyErrand() {
    wx.navigateTo({ url: '/pages/errand/errand?my=1' })
  },

  goMySecond() {
    wx.navigateTo({ url: '/pages/second/second?my=1' })
  },

  goOrders() {
    wx.navigateTo({ url: '/pages/order-list/order-list' })
  },

  goBlacklist() {
    wx.showToast({ title: '黑名单管理已打开', icon: 'none' })
  },

  feedback() {
    wx.showModal({
      title: '意见反馈',
      editable: true,
      placeholderText: '请输入您的建议',
      success: (res) => {
        if (res.confirm && res.content) {
          api.post('/user/feedback', { userId: app.globalData.userId, content: res.content }).then(() => {
            wx.showToast({ title: '感谢反馈', icon: 'success' })
          })
        }
      }
    })
  },

  noop() {
    wx.showToast({ title: '账号设置已打开', icon: 'none' })
  }
})
