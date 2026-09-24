const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    user: {},
    tagList: [],
    imgBase: '',
    album: [],
    previewUrl: '',
    targetId: null,
    showFollow: false,
    followed: false
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.id) {
      this.setData({ targetId: options.id })
      this.loadDetail(options.id)
    }
  },

  loadDetail(id) {
    const visitorId = app.globalData.userId
    const showFollow = !!(visitorId && String(visitorId) !== String(id))
    this.setData({ showFollow })
    let url = '/user/detail/' + id
    if (visitorId) url += '?visitorId=' + visitorId
    api.get(url).then((user) => {
      const ageText = user.age != null && user.age !== '' ? String(user.age) : ''
      this.setData({
        user,
        displayName: ageText ? `${user.nickname}，${ageText}` : user.nickname,
        tagList: user.tags ? user.tags.split(',') : []
      })
    })
    api.get('/user/photos/' + id).then((list) => {
      this.setData({ album: list.filter((p) => p.auditStatus === 1) })
    })
    if (showFollow) {
      api.get('/user/follow/status?userId=' + visitorId + '&targetId=' + id).then((res) => {
        this.setData({ followed: !!res.followed })
      })
    }
  },

  toggleFollow() {
    if (!app.checkLogin()) return
    const userId = app.globalData.userId
    const targetId = this.data.targetId
    if (!userId || !targetId) return
    if (this.data.followed) {
      api.del('/user/follow?userId=' + userId + '&targetId=' + targetId).then(() => {
        this.setData({ followed: false })
        wx.showToast({ title: '已取消关注', icon: 'none' })
      })
      return
    }
    api.post('/user/follow/add?userId=' + userId + '&targetId=' + targetId).then(() => {
      this.setData({ followed: true })
      wx.showToast({ title: '关注成功', icon: 'success' })
    })
  },

  previewPhoto(e) {
    this.setData({ previewUrl: e.currentTarget.dataset.url })
  },

  closePreview() {
    this.setData({ previewUrl: '' })
  }
})
