const api = require('../../utils/api')
const app = getApp()

function isValidIdCard(idCard) {
  const card = (idCard || '').trim().toUpperCase()
  if (!/^[1-9]\d{16}[\dX]$/.test(card)) return false
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const codes = '10X98765432'
  let sum = 0
  for (let i = 0; i < 17; i++) sum += parseInt(card[i], 10) * weights[i]
  return codes[sum % 11] === card[17]
}

Page({
  data: {
    phone: '',
    code: '',
    realName: '',
    idCard: '',
    agreed: false,
    smsBtnText: '获取验证码',
    smsBtnDisabled: false,
    codeExpireText: ''
  },

  onLoad() {
    if (!app.checkLogin()) {
      setTimeout(() => wx.navigateBack(), 300)
      return
    }
    api.get('/auth/status/' + app.globalData.userId).then((res) => {
      if (res.personalStatus === 2 || res.identityVerified === 1) {
        wx.showToast({ title: '您已完成个人认证', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else if (res.personalStatus === 1) {
        wx.showToast({ title: '个人认证审核中', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
      }
    }).catch(() => {})
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

  validateForm() {
    const d = this.data
    if (!/^1\d{10}$/.test(d.phone) || !d.code || !d.realName) {
      wx.showToast({ title: '请完整填写手机号、验证码和姓名', icon: 'none' })
      return false
    }
    if (!isValidIdCard(d.idCard)) {
      wx.showToast({ title: '身份证号码不正确，请核对18位号码', icon: 'none' })
      return false
    }
    return true
  },

  submit() {
    if (!this.ensureAgreed()) return
    if (!this.validateForm()) return
    const d = this.data
    api.post('/auth/personal/submit?smsCode=' + encodeURIComponent(d.code), {
      userId: app.globalData.userId,
      phone: d.phone,
      realName: d.realName,
      idCard: d.idCard
    }).then(() => {
      this.clearTimers()
      wx.showModal({
        title: '已提交审核',
        content: '个人认证资料已提交，管理员审核通过后您可发布 1 条动态，并在 2 小时内完成学校认证以保留发布权限。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
