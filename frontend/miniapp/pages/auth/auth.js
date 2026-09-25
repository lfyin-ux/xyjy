const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    personalStatus: 0,
    schoolStatus: 0,
    personalText: '待认证',
    schoolText: '待认证',
    personalReject: '',
    schoolReject: ''
  },

  onShow() {
    this.loadStatus()
  },

  loadStatus() {
    api.get('/auth/status/' + app.globalData.userId).then((res) => {
      const t = (s) => (s === 2 ? '已通过' : s === 1 ? '审核中' : s === 3 ? '未通过' : '待认证')
      this.setData({
        personalStatus: res.personalStatus,
        schoolStatus: res.schoolStatus,
        personalText: t(res.personalStatus),
        schoolText: t(res.schoolStatus),
        personalReject: res.personalReject || '',
        schoolReject: res.schoolReject || ''
      })
    })
  },

  goPersonal() {
    if (this.data.personalStatus === 2) {
      wx.showToast({ title: '个人认证已通过', icon: 'none' })
      return
    }
    if (this.data.personalStatus === 1) {
      wx.showToast({ title: '个人认证审核中', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/personal-auth/personal-auth' })
  },

  goSchool() {
    if (this.data.personalStatus !== 2) {
      wx.showToast({ title: '请先完成并通过个人认证', icon: 'none' })
      return
    }
    if (this.data.schoolStatus === 1) {
      wx.showToast({ title: '学校认证审核中', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/school-auth/school-auth' })
  }
})
