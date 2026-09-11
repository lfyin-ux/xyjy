const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    phone: '',
    code: '',
    realName: '',
    idCard: '',
    idFrontImg: '',
    idBackImg: '',
    imgBase: '',
    agreed: false
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  toggleAgree() {
    this.setData({ agreed: !this.data.agreed })
  },

  openPrivacy() {
    wx.navigateTo({ url: '/pages/privacy-policy/privacy-policy' })
  },

  ensureAgreed() {
    if (this.data.agreed) return true
    wx.showToast({ title: '请先同意隐私政策中的信息收集说明', icon: 'none' })
    return false
  },

  onPhone(e) { this.setData({ phone: e.detail.value }) },
  onCode(e) { this.setData({ code: e.detail.value }) },
  onName(e) { this.setData({ realName: e.detail.value }) },
  onIdCard(e) { this.setData({ idCard: e.detail.value }) },

  sendCode() {
    if (!this.ensureAgreed()) return
    if (!/^1\d{10}$/.test(this.data.phone)) {
      wx.showToast({ title: '请输入正确的手机号码', icon: 'none' })
      return
    }
    api.post('/auth/sendCode?phone=' + this.data.phone).then((code) => {
      const text = String(code || '')
      if (/^\d{6}$/.test(text)) {
        this.setData({ code: text })
        wx.showToast({ title: '测试验证码：' + text, icon: 'none' })
      } else {
        wx.showToast({ title: '验证码已发送', icon: 'none' })
      }
    })
  },

  uploadFront() {
    if (!this.ensureAgreed()) return
    this.chooseAndUpload('idFrontImg')
  },

  uploadBack() {
    if (!this.ensureAgreed()) return
    this.chooseAndUpload('idBackImg')
  },

  chooseAndUpload(field) {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const path = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中' })
        api.uploadFile(path, 'idcard').then((url) => {
          wx.hideLoading()
          this.setData({ [field]: url })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  submit() {
    if (!this.ensureAgreed()) return
    const d = this.data
    if (!/^1\d{10}$/.test(d.phone) || !d.code || !d.realName
      || !/^[1-9]\d{16}[\dXx]$/.test(d.idCard) || !d.idFrontImg || !d.idBackImg) {
      wx.showToast({ title: '请完整填写信息并上传身份证正反面', icon: 'none' })
      return
    }
    api.post('/auth/personal/submit?smsCode=' + encodeURIComponent(d.code), {
      userId: app.globalData.userId,
      phone: d.phone,
      realName: d.realName,
      idCard: d.idCard,
      idFrontImg: d.idFrontImg,
      idBackImg: d.idBackImg
    }).then(() => {
      wx.showModal({
        title: '材料已提交',
        content: '后台正在审核你的个人认证材料，审核通过后认证才会生效。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
