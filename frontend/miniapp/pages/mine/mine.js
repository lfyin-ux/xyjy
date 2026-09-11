const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    user: {},
    fullAccess: false,
    authSummary: '个人认证和学校认证均待完成',
    profileExtra: '',
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.loadUser()
  },

  loadUser() {
    const uid = app.globalData.userId
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
      this.setData({
        fullAccess: res.fullAccess,
        authSummary: res.fullAccess ? '个人认证和学校认证均已通过'
          : '个人认证：' + statusText(res.personalStatus) + ' · 学校认证：' + statusText(res.schoolStatus)
      })
    })
  },

  goAuth() {
    wx.navigateTo({ url: '/pages/auth/auth' })
  },

  goEditProfile() {
    wx.navigateTo({ url: '/pages/edit-profile/edit-profile' })
  },

  goAlbum() {
    wx.navigateTo({ url: '/pages/album/album' })
  },

  goMyPosts() {
    wx.navigateTo({ url: '/pages/post-detail/post-detail?my=1' })
  },

  goMyComments() {
    wx.navigateTo({ url: '/pages/my-comments/my-comments' })
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
    wx.navigateTo({ url: '/pages/blacklist/blacklist' })
  },

  goAddress() {
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
          wx.removeStorageSync('userInfo')
          wx.removeStorageSync('userId')
          // 跳转登录页
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
