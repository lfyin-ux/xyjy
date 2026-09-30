const api = require('../../utils/api')
const app = getApp()

const DOC_TYPES = ['学籍在线验证报告', '学历证书电子注册备案表']

Page({
  data: {
    schoolMode: 'select',
    schoolId: null,
    schoolName: '',
    schoolKeyword: '',
    schoolOptions: [],
    docTypes: DOC_TYPES,
    docType: DOC_TYPES[0],
    college: '',
    studentNo: '',
    docImg: '',
    docIsPdf: false,
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

  isPdfPath(url) {
    return (url || '').toLowerCase().indexOf('.pdf') !== -1
  },

  afterDocUploaded(url) {
    this.setData({
      docImg: url,
      docIsPdf: this.isPdfPath(url)
    })
  },

  uploadDoc() {
    if (!this.ensureAgreed()) return
    wx.showActionSheet({
      itemList: ['上传图片（截图/照片）', '上传 PDF 文件'],
      success: (res) => {
        if (res.tapIndex === 0) this.pickImageDoc()
        else if (res.tapIndex === 1) this.pickPdfDoc()
      }
    })
  },

  pickImageDoc() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const path = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中' })
        api.uploadFile(path, 'school').then((url) => {
          wx.hideLoading()
          this.afterDocUploaded(url)
        }).catch(() => wx.hideLoading())
      }
    })
  },

  pickPdfDoc() {
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['pdf'],
      success: (res) => {
        const path = res.tempFiles[0].path
        wx.showLoading({ title: '上传中' })
        api.uploadFile(path, 'school').then((url) => {
          wx.hideLoading()
          this.afterDocUploaded(url)
        }).catch(() => wx.hideLoading())
      }
    })
  },

  previewDoc() {
    const url = this.data.docImg
    if (!url) return
    if (this.data.docIsPdf) {
      const full = api.imgUrl(url)
      wx.downloadFile({
        url: full,
        success: (res) => {
          wx.openDocument({ filePath: res.tempFilePath, showMenu: true })
        },
        fail: () => wx.showToast({ title: '无法打开文件', icon: 'none' })
      })
    }
  },

  submit() {
    if (!this.ensureAgreed()) return
    const d = this.data
    if (!d.schoolName || !d.docImg) {
      wx.showToast({ title: '请选择学校并上传学信网材料', icon: 'none' })
      return
    }
    if (DOC_TYPES.indexOf(d.docType) < 0) {
      wx.showToast({ title: '请选择正确的材料类型', icon: 'none' })
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
        content: '后台将核对您上传的学信网报告，审核通过后学校认证才会生效。',
        showCancel: false,
        success: () => wx.navigateBack()
      })
    })
  }
})
