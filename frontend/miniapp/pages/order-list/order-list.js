const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    orders: [],
    status: null,
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.load()
  },

  load() {
    let url = '/mall/orders/' + app.globalData.userId
    if (this.data.status !== null) url += '?status=' + this.data.status
    api.get(url).then((orders) => this.setData({ orders }))
  },

  pick(e) {
    const s = e.currentTarget.dataset.s
    this.setData({ status: s === '' ? null : Number(s) })
    this.load()
  },

  pay(e) {
    api.post('/mall/order/pay/' + e.currentTarget.dataset.id).then(() => {
      wx.showToast({ title: '支付成功', icon: 'success' })
      this.load()
    })
  },

  cancel(e) {
    api.post('/mall/order/cancel/' + e.currentTarget.dataset.id).then(() => {
      wx.showToast({ title: '已取消', icon: 'none' })
      this.load()
    })
  },

  confirm(e) {
    api.post('/mall/order/confirm/' + e.currentTarget.dataset.id).then(() => {
      wx.showToast({ title: '已确认收货', icon: 'success' })
      this.load()
    })
  },

  refund(e) {
    api.post('/mall/order/refund/' + e.currentTarget.dataset.id).then(() => {
      wx.showToast({ title: '已申请售后', icon: 'none' })
      this.load()
    })
  }
})
