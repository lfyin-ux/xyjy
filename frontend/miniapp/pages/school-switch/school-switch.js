const api = require('../../utils/api')
const schoolContext = require('../../utils/schoolContext')
const app = getApp()

Page({
  data: {
    keyword: '',
    schoolOptions: [],
    homeSchoolId: null,
    homeSchoolName: '',
    currentSchoolId: null,
    currentSchoolName: '',
    isHomeSchool: true,
    viewOnly: false,
    mallTotalSpent: 0,
    unlockAmount: 2000,
    progressPercent: 0
  },

  onLoad() {
    this.loadContext()
    this.searchSchools('')
  },

  loadContext() {
    schoolContext.load(app).then((ctx) => {
      if (!ctx) return
      const spent = Number(ctx.mallTotalSpent) || 0
      const unlock = Number(ctx.unlockAmount) || 2000
      const progressPercent = Math.min(100, Math.round(spent / unlock * 100))
      this.setData({
        homeSchoolId: ctx.homeSchoolId,
        homeSchoolName: ctx.homeSchoolName || '',
        currentSchoolId: ctx.currentSchoolId,
        currentSchoolName: ctx.currentSchoolName,
        isHomeSchool: ctx.isHomeSchool,
        viewOnly: ctx.viewOnly,
        mallTotalSpent: spent,
        unlockAmount: unlock,
        progressPercent
      })
    })
  },

  onKeyword(e) {
    const kw = e.detail.value
    this.setData({ keyword: kw })
    this.searchSchools(kw)
  },

  searchSchools(keyword) {
    const url = '/common/schools' + (keyword ? '?keyword=' + encodeURIComponent(keyword) : '')
    api.get(url).then((list) => {
      this.setData({ schoolOptions: list || [] })
    })
  },

  pickSchool(e) {
    const schoolId = e.currentTarget.dataset.id
    const schoolName = e.currentTarget.dataset.name
    if (!schoolId || !app.globalData.userId) return
    wx.showLoading({ title: '切换中' })
    api.post('/user/school/switch?userId=' + app.globalData.userId + '&schoolId=' + schoolId).then((ctx) => {
      wx.hideLoading()
      app.globalData.schoolContext = ctx
      wx.setStorageSync('schoolContext', ctx)
      wx.showToast({ title: '已切换到' + schoolName, icon: 'none' })
      const spent = Number(ctx.mallTotalSpent) || 0
      const unlock = Number(ctx.unlockAmount) || 2000
      this.setData({
        currentSchoolId: ctx.currentSchoolId,
        currentSchoolName: ctx.currentSchoolName,
        isHomeSchool: ctx.isHomeSchool,
        viewOnly: ctx.viewOnly,
        mallTotalSpent: spent,
        unlockAmount: unlock,
        progressPercent: Math.min(100, Math.round(spent / unlock * 100))
      })
      setTimeout(() => wx.navigateBack(), 500)
    }).catch(() => wx.hideLoading())
  },

  backHome() {
    const homeId = this.data.homeSchoolId
    if (!homeId) return
    const name = this.data.homeSchoolName || '所属学校'
    this.pickSchool({ currentTarget: { dataset: { id: homeId, name } } })
  }
})
