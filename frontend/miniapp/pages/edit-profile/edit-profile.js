const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    form: {},
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadUser()
  },

  loadUser() {
    api.get('/user/detail/' + app.globalData.userId).then((user) => {
      this.setData({ form: user })
    })
  },

  uploadAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        wx.showLoading({ title: '上传中' })
        api.uploadFile(res.tempFiles[0].tempFilePath, 'avatar').then((url) => {
          wx.hideLoading()
          this.setData({ 'form.avatar': url })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  onNickname(e) { this.setData({ 'form.nickname': e.detail.value }) },
  onGender(e) { this.setData({ 'form.gender': Number(e.detail.value) }) },
  onAge(e) { this.setData({ 'form.age': Number(e.detail.value) }) },
  onHeight(e) { this.setData({ 'form.height': Number(e.detail.value) }) },
  onIntro(e) { this.setData({ 'form.intro': e.detail.value }) },
  onTags(e) { this.setData({ 'form.tags': e.detail.value }) },
  onPartner(e) { this.setData({ 'form.partnerType': e.detail.value }) },
  onExpect(e) { this.setData({ 'form.expect': e.detail.value }) },

  save() {
    api.post('/user/update', this.data.form).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 700)
    })
  }
})
