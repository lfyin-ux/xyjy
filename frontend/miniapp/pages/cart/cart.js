const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    items: [],
    total: 0,
    imgBase: '',
    selectedAddress: null
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadDefaultAddress()
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

  loadDefaultAddress() {
    api.get('/address/default/' + app.globalData.userId).then((addr) => {
      if (addr) {
        this.setData({ selectedAddress: addr })
      }
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

  chooseAddress() {
    wx.navigateTo({ url: '/pages/address/address?select=1' })
  },

  settle() {
    const addr = this.data.selectedAddress
    if (!addr) {
      wx.showToast({ title: '请先选择收货地址', icon: 'none' })
      return
    }
    const items = this.data.items.map((it) => ({
      goodsId: it.cart.goodsId,
      quantity: it.cart.quantity,
      spec: it.cart.spec
    }))
    api.post('/mall/order/submit', {
      userId: app.globalData.userId,
      address: addr.address,
      receiver: addr.receiver,
      phone: addr.phone,
      items
    }).then((order) => {
      // 调用支付接口
      api.post('/pay/create/' + order.id).then((payResult) => {
        if (payResult.mode === 'dev') {
          // 开发模式 直接支付成功 清空购物车
          this.data.items.forEach((it) => api.del('/mall/cart/' + it.cart.id))
          wx.showToast({ title: '下单支付成功', icon: 'success' })
          setTimeout(() => wx.navigateTo({ url: '/pages/order-list/order-list' }), 800)
        } else {
          // 生产模式 拉起微信支付
          wx.requestPayment({
            timeStamp: payResult.timeStamp,
            nonceStr: payResult.nonceStr,
            package: payResult.package,
            signType: payResult.signType,
            paySign: payResult.paySign,
            success: () => {
              this.data.items.forEach((it) => api.del('/mall/cart/' + it.cart.id))
              wx.showToast({ title: '支付成功', icon: 'success' })
              setTimeout(() => wx.navigateTo({ url: '/pages/order-list/order-list' }), 800)
            },
            fail: () => {
              wx.showToast({ title: '支付取消', icon: 'none' })
            }
          })
        }
      })
    })
  }
})
