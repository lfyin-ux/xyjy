const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    orderId: null,
    order: {},
    contact: ''
  },

  onLoad(options) {
    this.setData({ orderId: options.id })
    this.loadOrder(options.id)
  },

  loadOrder(id) {
    const uid = app.globalData.userId
    const url = '/errand/detail/' + id + (uid ? '?userId=' + uid : '')
    api.get(url).then((order) => {
      this.setData({ order })
    })
  },

  onContact(e) {
    this.setData({ contact: e.detail.value })
  },

  confirm() {
    if (!app.checkLogin()) return
    if (!this.data.contact.trim()) {
      wx.showToast({ title: '请填写联系方式', icon: 'none' })
      return
    }
    api.post('/errand/accept?id=' + this.data.orderId + '&takerId=' + app.globalData.userId + '&contact=' + encodeURIComponent(this.data.contact)).then(() => {
      wx.showToast({ title: '接单成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 700)
    })
  }
})
