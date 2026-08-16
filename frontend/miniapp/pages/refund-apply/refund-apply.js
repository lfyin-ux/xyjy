const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    orderId: null,
    items: [],
    reason: '',
    refundTotal: '0.00'
  },

  onLoad(options) {
    this.setData({ orderId: options.orderId })
    this.loadItems()
  },

  // 加载订单明细
  loadItems() {
    api.get('/mall/orders/' + app.globalData.userId + '?status=').then((orders) => {
      // 找到对应订单
      const order = orders.find((o) => String(o.order.id) === String(this.data.orderId))
      if (order && order.items) {
        const items = order.items.map((it) => ({
          id: it.id,
          goodsName: it.goodsName,
          spec: it.spec,
          price: it.price,
          quantity: it.quantity,
          checked: false,
          refundQty: 1
        }))
        this.setData({ items })
      }
    })
  },

  // 勾选/取消
  toggle(e) {
    const idx = e.currentTarget.dataset.idx
    const key = 'items[' + idx + '].checked'
    this.setData({ [key]: !this.data.items[idx].checked })
    this.calcTotal()
  },

  minus(e) {
    const idx = e.currentTarget.dataset.idx
    const item = this.data.items[idx]
    if (item.refundQty > 1) {
      this.setData({ ['items[' + idx + '].refundQty']: item.refundQty - 1 })
      this.calcTotal()
    }
  },

  plus(e) {
    const idx = e.currentTarget.dataset.idx
    const item = this.data.items[idx]
    if (item.refundQty < item.quantity) {
      this.setData({ ['items[' + idx + '].refundQty']: item.refundQty + 1 })
      this.calcTotal()
    }
  },

  onReason(e) {
    this.setData({ reason: e.detail.value })
  },

  calcTotal() {
    let total = 0
    this.data.items.forEach((it) => {
      if (it.checked) {
        total += it.price * it.refundQty
      }
    })
    this.setData({ refundTotal: total.toFixed(2) })
  },

  submit() {
    const selected = this.data.items.filter((it) => it.checked)
    if (selected.length === 0) {
      wx.showToast({ title: '请选择要退款的商品', icon: 'none' })
      return
    }
    const items = selected.map((it) => ({
      itemId: it.id,
      quantity: it.refundQty,
      reason: this.data.reason
    }))
    api.post('/refund/apply', {
      orderId: Number(this.data.orderId),
      items: items
    }).then(() => {
      wx.showToast({ title: '退款申请已提交', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 800)
    })
  }
})
