const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    items: [],
    total: 0,
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.load()
  },

  load() {
    api.get('/mall/cart/' + app.globalData.userId).then((items) => {
      this.setData({ items })
      this.calcTotal()
    })
  },

  calcTotal() {
    let total = 0
    this.data.items.forEach((it) => {
      if (it.goods) total += it.goods.price * it.cart.quantity
    })
    this.setData({ total: total.toFixed(2) })
  },

  minus(e) {
    const cart = e.currentTarget.dataset.item
    if (cart.quantity <= 1) return
    api.post('/mall/cart/update?id=' + cart.id + '&quantity=' + (cart.quantity - 1)).then(() => this.load())
  },

  plus(e) {
    const cart = e.currentTarget.dataset.item
    api.post('/mall/cart/update?id=' + cart.id + '&quantity=' + (cart.quantity + 1)).then(() => this.load())
  },

  del(e) {
    const id = e.currentTarget.dataset.id
    api.del('/mall/cart/' + id).then(() => {
      wx.showToast({ title: '已删除', icon: 'none' })
      this.load()
    })
  },

  settle() {
    const items = this.data.items.map((it) => ({
      goodsId: it.cart.goodsId,
      quantity: it.cart.quantity,
      spec: it.cart.spec
    }))
    api.post('/mall/order/submit', {
      userId: app.globalData.userId,
      address: '华南理工大学',
      receiver: (app.globalData.userInfo && app.globalData.userInfo.nickname) || '同学',
      phone: (app.globalData.userInfo && app.globalData.userInfo.phone) || '',
      items
    }).then((order) => {
      api.post('/mall/order/pay/' + order.id).then(() => {
        // 清空购物车项
        this.data.items.forEach((it) => api.del('/mall/cart/' + it.cart.id))
        wx.showToast({ title: '下单支付成功', icon: 'success' })
        setTimeout(() => wx.navigateTo({ url: '/pages/order-list/order-list' }), 800)
      })
    })
  }
})
