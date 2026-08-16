const api = require('../../utils/api')

Page({
  data: {
    refunds: []
  },

  onLoad(options) {
    if (options.orderId) {
      this.loadRefunds(options.orderId)
    }
  },

  loadRefunds(orderId) {
    api.get('/refund/order/' + orderId).then((list) => {
      this.setData({ refunds: list })
    })
  }
})
