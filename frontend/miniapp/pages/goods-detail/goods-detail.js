const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    goods: {},
    specList: [],
    spec: '',
    quantity: 1,
    imgBase: ''
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadGoods(options.id)
  },

  loadGoods(id) {
    api.get('/mall/goods/' + id).then((goods) => {
      const specList = goods.spec ? goods.spec.split(',') : []
      this.setData({
        goods,
        specList,
        spec: specList.length ? specList[0] : ''
      })
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
    api.post('/mall/cart/add', {
      userId: app.globalData.userId,
      goodsId: this.data.goods.id,
      spec: this.data.spec,
      quantity: this.data.quantity
    }).then(() => {
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    })
  },

  buyNow() {
    api.post('/mall/order/submit', {
      userId: app.globalData.userId,
      address: '华南理工大学',
      receiver: (app.globalData.userInfo && app.globalData.userInfo.nickname) || '同学',
      phone: (app.globalData.userInfo && app.globalData.userInfo.phone) || '',
      items: [{
        goodsId: this.data.goods.id,
        quantity: this.data.quantity,
        spec: this.data.spec
      }]
    }).then((order) => {
      // 演示直接支付
      api.post('/mall/order/pay/' + order.id).then(() => {
        wx.showToast({ title: '下单支付成功', icon: 'success' })
        setTimeout(() => wx.navigateTo({ url: '/pages/order-list/order-list' }), 800)
      })
    })
  }
})
