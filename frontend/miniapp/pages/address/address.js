const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [],
    editing: false,
    form: {},
    selectMode: false
  },

  onLoad(options) {
    // selectMode下 点击地址直接返回选中结果
    if (options.select) {
      this.setData({ selectMode: true })
    }
  },

  onShow() {
    this.load()
  },

  load() {
    api.get('/address/list/' + app.globalData.userId).then((list) => {
      this.setData({ list })
    })
  },

  showEdit() {
    this.setData({ editing: true, form: { userId: app.globalData.userId, isDefault: 0 } })
  },

  edit(e) {
    this.setData({ editing: true, form: { ...e.currentTarget.dataset.item } })
  },

  cancelEdit() {
    this.setData({ editing: false, form: {} })
  },

  onReceiver(e) { this.setData({ 'form.receiver': e.detail.value }) },
  onPhone(e) { this.setData({ 'form.phone': e.detail.value }) },
  onAddress(e) { this.setData({ 'form.address': e.detail.value }) },
  onDefault(e) { this.setData({ 'form.isDefault': e.detail.value ? 1 : 0 }) },

  saveAddr() {
    const f = this.data.form
    if (!f.receiver || !f.phone || !f.address) {
      wx.showToast({ title: '请完整填写地址信息', icon: 'none' })
      return
    }
    api.post('/address/save', f).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.setData({ editing: false, form: {} })
      this.load()
    })
  },

  setDefault(e) {
    api.post('/address/setDefault/' + e.currentTarget.dataset.id).then(() => {
      wx.showToast({ title: '已设为默认', icon: 'success' })
      this.load()
    })
  },

  del(e) {
    wx.showModal({
      title: '提示',
      content: '确定删除该地址吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/address/' + e.currentTarget.dataset.id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' })
            this.load()
          })
        }
      }
    })
  },

  // 选择模式 点击地址返回给上一页
  selectAddr(e) {
    const item = e.currentTarget.dataset.item
    const pages = getCurrentPages()
    const prevPage = pages[pages.length - 2]
    if (prevPage) {
      prevPage.setData({ selectedAddress: item })
    }
    wx.navigateBack()
  }
})
