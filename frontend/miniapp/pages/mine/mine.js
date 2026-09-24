const api = require('../../utils/api')
const schoolContext = require('../../utils/schoolContext')
const violationNotice = require('../../utils/violationNotice')
const app = getApp()

Page({
  data: {
    loggedIn: false,
    user: {},
    fullAccess: false,
    authPassed: false,
    authSummary: '个人认证和学校认证均待完成',
    profileExtra: '',
    imgBase: '',
    schoolContext: {},
    violationUnread: 0
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setData({ selected: 3 })
    this.loadUser()
  },

  loadUser() {
    const uid = app.globalData.userId
    if (!uid) {
      this.setData({ loggedIn: false, user: {}, fullAccess: false, authPassed: false, profileExtra: '', violationUnread: 0 })
      return
    }
    this.setData({ loggedIn: true })
    violationNotice.loadUnreadCount(uid).then((count) => {
      this.setData({ violationUnread: count })
      violationNotice.checkAndPrompt(uid)
    })
    schoolContext.load(app).then((ctx) => {
      if (ctx) this.setData({ schoolContext: ctx })
    })
    api.get('/user/detail/' + uid).then((user) => {
      const extra = []
      if (user.gender === 1) extra.push('♂')
      else if (user.gender === 2) extra.push('♀')
      if (user.age != null && user.age !== '') extra.push(String(user.age))
      this.setData({
        user,
        profileExtra: extra.join(' ')
      })
    })
    // 认证状态
    api.get('/auth/status/' + uid).then((res) => {
      const statusText = (s) => (s === 2 ? '已通过' : s === 1 ? '审核中' : s === 3 ? '未通过' : '待认证')
      const authPassed = res.identityVerified === 1 && res.schoolVerified === 1
      this.setData({
        fullAccess: res.fullAccess,
        authPassed,
        authSummary: authPassed ? '个人认证和学校认证均已通过'
          : '个人认证：' + statusText(res.personalStatus) + ' · 学校认证：' + statusText(res.schoolStatus)
      })
    })
  },

  goSchoolSwitch() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/school-switch/school-switch' })
  },

  goAuth() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/auth/auth' })
  },

  goEditProfile() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/edit-profile/edit-profile' })
  },

  goAlbum() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/album/album' })
  },

  goMyFollows() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/my-follows/my-follows' })
  },

  goViolations() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/violation-notices/violation-notices' })
  },

  goMyPosts() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/post-detail/post-detail?my=1' })
  },

  goMyComments() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/my-comments/my-comments' })
  },

  goMyErrand() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/errand/errand?my=1' })
  },

  goMySecond() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/second/second?my=1' })
  },

  goOrders() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/order-list/order-list' })
  },

  goAddress() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/address/address' })
  },

  goUserAgreement() {
    wx.navigateTo({ url: '/pages/user-agreement/user-agreement' })
  },

  goPrivacyPolicy() {
    wx.navigateTo({ url: '/pages/privacy-policy/privacy-policy' })
  },

  feedback() {
    wx.navigateTo({ url: '/pages/feedback/feedback' })
  },

  goLogin() {
    app.checkLogin()
  },

  noop() {
    wx.showToast({ title: '账号设置已打开', icon: 'none' })
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？退出后可切换其他用户登录',
      success: (res) => {
        if (res.confirm) {
          // 清除登录状态
          app.globalData.userInfo = null
          app.globalData.userId = null
          app.globalData.schoolContext = null
          wx.removeStorageSync('userInfo')
          wx.removeStorageSync('userId')
          wx.removeStorageSync('schoolContext')
          // 退出后回到可游客浏览的首页
          wx.switchTab({ url: '/pages/match/match' })
        }
      }
    })
  }
})
