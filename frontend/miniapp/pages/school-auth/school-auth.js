const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    schoolMode: 'select',
    schoolId: null,
    schoolName: '',
    schoolKeyword: '',
    schoolOptions: [],
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
    this.searchSchools('')
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

  switchToSelect() {
    this.setData({ schoolMode: 'select', schoolId: null, schoolName: '', schoolKeyword: '' })
    this.searchSchools('')
  },

  switchToManual() {
    this.setData({ schoolMode: 'manual', schoolId: null, schoolName: '', schoolKeyword: '' })
  },

  onSchoolKeyword(e) {
    const kw = e.detail.value
    this.setData({ schoolKeyword: kw })
    this.searchSchools(kw)
  },

  searchSchools(keyword) {
    const url = '/common/schools' + (keyword ? '?keyword=' + encodeURIComponent(keyword) : '')
    api.get(url).then((list) => {
      this.setData({ schoolOptions: list || [] })
    }).catch(() => {
      this.setData({ schoolOptions: [] })
    })
  },

  pickSchool(e) {
    const item = e.currentTarget.dataset.item
    if (!item) return
    this.setData({
      schoolId: item.id,
      schoolName: item.schoolName,
      schoolKeyword: item.schoolName
    })
  },

  onSchoolManual(e) {
    this.setData({ schoolName: e.detail.value, schoolId: null })
  },

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
      wx.showToast({ title: '请选择或输入学校并上传证明材料', icon: 'none' })
      return
    }
    const payload = {
      userId: app.globalData.userId,
      schoolName: d.schoolName,
      college: d.college,
      studentNo: d.studentNo,
      docType: d.docType,
      docImgs: d.docImg,
      remark: d.remark
    }
    if (d.schoolMode === 'select' && d.schoolId) {
      payload.schoolId = d.schoolId
    }
    api.post('/auth/school/submit', payload).then(() => {
      wx.showModal({
        title: '材料已提交',
        content: '后台正在审核你的学校认证材料，审核通过后认证才会生效。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
