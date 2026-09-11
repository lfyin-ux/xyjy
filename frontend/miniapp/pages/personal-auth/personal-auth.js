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
    agreed: false,
    smsBtnText: '获取验证码',
    smsBtnDisabled: false,
    codeExpireText: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onUnload() {
    this.clearTimers()
  },

  clearTimers() {
    if (this._resendTimer) {
      clearInterval(this._resendTimer)
      this._resendTimer = null
    }
    if (this._expireTimer) {
      clearInterval(this._expireTimer)
      this._expireTimer = null
    }
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

  startResendCountdown(seconds) {
    let left = seconds
    this.setData({ smsBtnDisabled: true, smsBtnText: left + 's后重发' })
    this._resendTimer = setInterval(() => {
      left -= 1
      if (left <= 0) {
        clearInterval(this._resendTimer)
        this._resendTimer = null
        this.setData({ smsBtnDisabled: false, smsBtnText: '获取验证码' })
        return
      }
      this.setData({ smsBtnText: left + 's后重发' })
    }, 1000)
  },

  startExpireCountdown(seconds) {
    let left = seconds
    const format = (s) => {
      const m = Math.floor(s / 60)
      const sec = s % 60
      return (m < 10 ? '0' + m : '' + m) + ':' + (sec < 10 ? '0' + sec : '' + sec)
    }
    this.setData({ codeExpireText: '验证码 ' + format(left) + ' 内有效' })
    this._expireTimer = setInterval(() => {
      left -= 1
      if (left <= 0) {
        clearInterval(this._expireTimer)
        this._expireTimer = null
        this.setData({ codeExpireText: '验证码已过期，请重新获取', code: '' })
        return
      }
      this.setData({ codeExpireText: '验证码 ' + format(left) + ' 内有效' })
    }, 1000)
  },

  sendCode() {
    if (!this.ensureAgreed()) return
    if (this.data.smsBtnDisabled) return
    if (!/^1\d{10}$/.test(this.data.phone)) {
      wx.showToast({ title: '请输入正确的手机号码', icon: 'none' })
      return
    }
    api.post('/auth/sendCode?phone=' + this.data.phone).then((res) => {
      const data = res && typeof res === 'object' ? res : { code: res }
      const expireSeconds = data.expireSeconds || 300
      const expireMinutes = data.expireMinutes || 5
      if (data.code) {
        this.setData({ code: String(data.code) })
        wx.showToast({ title: '测试验证码：' + data.code, icon: 'none' })
      } else {
        wx.showToast({ title: data.message || ('验证码已发送，' + expireMinutes + '分钟内有效'), icon: 'none' })
      }
      this.clearTimers()
      this.startResendCountdown(60)
      this.startExpireCountdown(expireSeconds)
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
      this.clearTimers()
      wx.showModal({
        title: '材料已提交',
        content: '后台正在审核你的个人认证材料，审核通过后认证才会生效。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
