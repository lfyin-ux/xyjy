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
    api.get('/errand/detail/' + id).then((order) => {
      this.setData({ order })
    })
  },

  onContact(e) {
    this.setData({ contact: e.detail.value })
  },

  confirm() {
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
