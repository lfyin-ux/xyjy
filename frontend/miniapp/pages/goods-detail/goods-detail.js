const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    goods: {},
    imageList: [],
    specList: [],
    spec: '',
    quantity: 1,
    imgBase: '',
    selectedAddress: null
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadGoods(options.id)
    if (app.globalData.userId) this.loadDefaultAddress()
  },

  onShow() {
    // 从地址选择页返回时 selectedAddress 已被设置
  },

  loadGoods(id) {
    api.get('/mall/goods/' + id).then((goods) => {
      const specList = goods.spec ? goods.spec.split(',') : []
      const imageList = []
      if (goods.cover) imageList.push(goods.cover)
      if (goods.images) {
        goods.images.split(',').forEach((url) => {
          const u = (url || '').trim()
          if (u && imageList.indexOf(u) === -1) imageList.push(u)
        })
      }
      this.setData({
        goods,
        imageList,
        specList,
        spec: specList.length ? specList[0] : ''
      })
    })
  },

  // 加载默认收货地址
  loadDefaultAddress() {
    api.get('/address/default/' + app.globalData.userId).then((addr) => {
      if (addr) {
        this.setData({ selectedAddress: addr })
      }
    })
  },

  pickSpec(e) {
    this.setData({ spec: e.currentTarget.dataset.s })
  },

  minus() {
    if (this.data.quantity > 1) {
      this.setData({ quantity: this.data.quantity - 1 })
    }
  },

  plus() {
    this.setData({ quantity: this.data.quantity + 1 })
  },

  addCart() {
    if (!app.checkLogin()) return
    api.post('/mall/cart/add', {
      userId: app.globalData.userId,
      goodsId: this.data.goods.id,
      spec: this.data.spec,
      quantity: this.data.quantity
    }).then(() => {
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    })
  },

  // 选择收货地址
  chooseAddress() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/address/address?select=1' })
  },

  buyNow() {
    if (!app.checkLogin()) return
    const addr = this.data.selectedAddress
    if (!addr) {
      wx.showToast({ title: '请先选择收货地址', icon: 'none' })
      return
    }
    api.post('/mall/order/submit', {
      userId: app.globalData.userId,
      address: addr.address,
      receiver: addr.receiver,
      phone: addr.phone,
      items: [{
        goodsId: this.data.goods.id,
        quantity: this.data.quantity,
        spec: this.data.spec
      }]
    }).then((order) => {
      // 调用支付接口
      api.post('/pay/create/' + order.id).then((payResult) => {
        if (payResult.mode === 'dev') {
          // 开发模式 直接支付成功
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
