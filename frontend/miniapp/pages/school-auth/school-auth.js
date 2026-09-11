const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    schoolName: '',
    docTypes: ['录取通知书', '学生证', '校园卡', '学位证', '毕业证', '学信网证明/截图'],
    docType: '学生证',
    college: '',
    studentNo: '',
    docImg: '',
    remark: '',
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

  onSchool(e) { this.setData({ schoolName: e.detail.value }) },
  onCollege(e) { this.setData({ college: e.detail.value }) },
  onStudentNo(e) { this.setData({ studentNo: e.detail.value }) },
  onRemark(e) { this.setData({ remark: e.detail.value }) },
  onDocType(e) { this.setData({ docType: this.data.docTypes[e.detail.value] }) },

  uploadDoc() {
    if (!this.ensureAgreed()) return
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const path = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中' })
        api.uploadFile(path, 'school').then((url) => {
          wx.hideLoading()
          this.setData({ docImg: url })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  submit() {
    if (!this.ensureAgreed()) return
    const d = this.data
    if (!d.schoolName || !d.docImg) {
      wx.showToast({ title: '请输入学校并上传证明材料', icon: 'none' })
      return
    }
    api.post('/auth/school/submit', {
      userId: app.globalData.userId,
      schoolName: d.schoolName,
      college: d.college,
      studentNo: d.studentNo,
      docType: d.docType,
      docImgs: d.docImg,
      remark: d.remark
    }).then(() => {
      wx.showModal({
        title: '材料已提交',
        content: '后台正在审核你的学校认证材料，审核通过后认证才会生效。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
