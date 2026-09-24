const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [],
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    if (!app.globalData.userId) {
      this.setData({ list: [] })
      return
    }
    this.load()
  },

  load() {
    api.get('/user/follow/list/' + app.globalData.userId).then((list) => {
      this.setData({ list: list || [] })
    })
  },

  openProfile(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: '/pages/profile/profile?id=' + id })
  },

  unfollow(e) {
    const targetId = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定取消关注该用户吗？',
      success: (res) => {
        if (!res.confirm) return
        api.del('/user/follow?userId=' + app.globalData.userId + '&targetId=' + targetId).then(() => {
          wx.showToast({ title: '已取消关注', icon: 'success' })
          this.load()
        })
      }
    })
  }
})
