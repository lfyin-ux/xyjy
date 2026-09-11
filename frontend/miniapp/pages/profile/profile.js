const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    user: {},
    tagList: [],
    imgBase: '',
    album: [],
    previewUrl: ''
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.id) {
      this.setData({ targetId: options.id })
      this.loadDetail(options.id)
    }
  },

  loadDetail(id) {
    api.get('/user/detail/' + id).then((user) => {
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
  },

  previewPhoto(e) {
    this.setData({ previewUrl: e.currentTarget.dataset.url })
  },

  closePreview() {
    this.setData({ previewUrl: '' })
  }
})
