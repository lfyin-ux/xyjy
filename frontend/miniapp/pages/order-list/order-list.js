const api = require('../../utils/api')
const schoolContext = require('../../utils/schoolContext')
const app = getApp()

Page({
  data: {
    orders: [],
    status: null,
    imgBase: ''
  },

  onLoad(options) {
    const data = { imgBase: app.globalData.baseUrl }
    // 从商城订单入口跳转时带上状态 默认选中对应标签
    if (options && options.status) {
      data.status = Number(options.status)
    }
    this.setData(data)
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
    const orderId = e.currentTarget.dataset.id
    api.post('/pay/create/' + orderId).then((payResult) => {
      if (payResult.mode === 'dev') {
        wx.showToast({ title: '支付成功', icon: 'success' })
        schoolContext.load(app)
        this.load()
      } else {
        wx.requestPayment({
          timeStamp: payResult.timeStamp,
          nonceStr: payResult.nonceStr,
          package: payResult.package,
          signType: payResult.signType,
          paySign: payResult.paySign,
          success: () => {
            wx.showToast({ title: '支付成功', icon: 'success' })
            schoolContext.load(app)
            this.load()
          },
          fail: () => {
            wx.showToast({ title: '支付取消', icon: 'none' })
          }
        })
      }
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
      schoolContext.load(app)
      this.load()
    })
  },

  refund(e) {
    const orderId = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/refund-apply/refund-apply?orderId=' + orderId })
  },

  viewRefund(e) {
    const orderId = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/refund-detail/refund-detail?orderId=' + orderId })
  }
})
