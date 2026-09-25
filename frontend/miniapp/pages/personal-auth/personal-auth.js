const api = require('../../utils/api')
const app = getApp()

// 测试开关：true 时人脸核身仅需姓名+身份证，不要求手机号验证码
const FACE_VERIFY_SKIP_PHONE = true

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
    codeExpireText: '',
    faceVerifyNo: '',
    faceVerified: false,
    faceVerifying: false,
    facePreview: '',
    faceError: ''
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
  onName(e) {
    this.resetFaceIfIdentityChanged()
    this.setData({ realName: e.detail.value })
  },
  onIdCard(e) {
    this.resetFaceIfIdentityChanged()
    this.setData({ idCard: e.detail.value })
  },

  resetFaceIfIdentityChanged() {
    if (this.data.faceVerified || this.data.faceVerifyNo) {
      this.setData({
        faceVerifyNo: '',
        faceVerified: false,
        facePreview: ''
      })
    }
  },

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

  validateIdentityForFace() {
    const d = this.data
    if (!d.realName) {
      wx.showToast({ title: '请先填写真实姓名', icon: 'none' })
      return false
    }
    if (!isValidIdCard(d.idCard)) {
      wx.showToast({ title: '身份证号码不正确，请核对18位号码', icon: 'none' })
      return false
    }
    return true
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

  uploadFacePhoto(filePath) {
    const d = this.data
    if (!app.globalData.userId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return Promise.reject(new Error('not login'))
    }
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: api.getBaseUrl() + '/auth/personal/face/verify',
        filePath,
        name: 'file',
        formData: {
          userId: String(app.globalData.userId),
          realName: d.realName,
          idCard: d.idCard,
          phone: d.phone || ''
        },
        success: (res) => {
          if (res.statusCode === 404) {
            const tip = '核身接口不存在，请部署新后端或改连本地'
            this.setData({ faceError: tip })
            wx.showModal({ title: '人脸核身失败', content: tip, showCancel: false })
            reject(new Error(tip))
            return
          }
          let data = {}
          try {
            data = JSON.parse(res.data)
          } catch (e) {
            const tip = '服务器响应异常，请检查 baseUrl 是否指向正确后端'
            this.setData({ faceError: tip })
            wx.showToast({ title: tip, icon: 'none', duration: 3000 })
            reject(e)
            return
          }
          if (data.code === 200 && data.data) {
            this.setData({ faceError: '' })
            resolve(data.data)
          } else {
            const tip = (data && data.msg) || ('人脸核身失败(' + (res.statusCode || '') + ')')
            this.setData({ faceError: tip })
            wx.showToast({ title: tip, icon: 'none', duration: 3000 })
            reject(data)
          }
        },
        fail: (err) => {
          const tip = '上传失败，请检查网络、baseUrl 及「不校验合法域名」'
          this.setData({ faceError: tip })
          wx.showToast({ title: tip, icon: 'none', duration: 3000 })
          reject(err)
        }
      })
    })
  },

  startFaceVerify() {
    if (!this.ensureAgreed()) return
    const valid = FACE_VERIFY_SKIP_PHONE ? this.validateIdentityForFace() : this.validateForm()
    if (!valid) return
    if (this.data.faceVerifying) return

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['camera'],
      camera: 'front',
      success: (chooseRes) => {
        const tempPath = chooseRes.tempFiles && chooseRes.tempFiles[0] && chooseRes.tempFiles[0].tempFilePath
        if (!tempPath) {
          wx.showToast({ title: '未获取到照片', icon: 'none' })
          return
        }
        this.setData({
          faceVerifying: true,
          faceVerifyNo: '',
          faceVerified: false,
          facePreview: tempPath,
          faceError: ''
        })
        wx.showLoading({ title: '核身中' })
        const compressAndUpload = (path) => {
          this.uploadFacePhoto(path).then((res) => {
            wx.hideLoading()
            this.setData({
              faceVerifyNo: res.faceVerifyNo,
              faceVerified: true,
              faceVerifying: false
            })
            wx.showToast({ title: '人脸核身完成', icon: 'success' })
          }).catch(() => {
            wx.hideLoading()
            this.setData({ faceVerifying: false })
          })
        }
        if (wx.compressImage) {
          wx.compressImage({
            src: tempPath,
            quality: 70,
            success: (cmp) => compressAndUpload(cmp.tempFilePath || tempPath),
            fail: () => compressAndUpload(tempPath)
          })
        } else {
          compressAndUpload(tempPath)
        }
      },
      fail: (err) => {
        if (err && err.errMsg && err.errMsg.indexOf('cancel') === -1) {
          wx.showToast({ title: '无法打开相机', icon: 'none' })
        }
      }
    })
  },

  submit() {
    if (!this.ensureAgreed()) return
    if (!this.validateForm()) return
    if (!this.data.faceVerified || !this.data.faceVerifyNo) {
      wx.showToast({ title: '请先完成人脸核身', icon: 'none' })
      return
    }
    const d = this.data
    api.post('/auth/personal/submit?smsCode=' + encodeURIComponent(d.code)
      + '&faceVerifyNo=' + encodeURIComponent(d.faceVerifyNo), {
      userId: app.globalData.userId,
      phone: d.phone,
      realName: d.realName,
      idCard: d.idCard,
      faceVerifyNo: d.faceVerifyNo
    }).then(() => {
      this.clearTimers()
      wx.showModal({
        title: '个人认证已完成',
        content: '人脸核身已通过，个人认证已生效，你现在可以继续完成学校认证。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
