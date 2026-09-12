const api = require('../../utils/api')
const authGate = require('../../utils/authGate')
const app = getApp()

Page({
  data: {
    categories: [],
    goods: [],
    keyword: '',
    categoryId: null,
    cartCount: 0,
    orderStat: { unpaid: 0, prepare: 0, shipping: 0, aftersale: 0 },
    imgBase: '',
    locked: false
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadCategories()
  },

  onShow() {
    this.checkAuth()
  },

  checkAuth() {
    const userId = app.globalData.userId
    authGate.checkFullAccess(userId, () => {
      this.setData({ locked: false })
      this.loadGoods()
      this.loadCart()
      this.loadOrderStat()
    }, () => {
      this.setData({ locked: true })
    })
    /* TODO: 审核通过后恢复双认证门禁（并将 authGate.js 中 AUTH_GATE_ENABLED 改为 true）
    if (!userId) {
      this.setData({ locked: true })
      return
    }
    api.get('/auth/status/' + userId).then((res) => {
      if (res.fullAccess) {
        this.setData({ locked: false })
        this.loadGoods()
        this.loadCart()
        this.loadOrderStat()
      } else {
        this.setData({ locked: true })
      }
    }).catch(() => {
      this.setData({ locked: true })
    })
    */
  },

  loadCategories() {
    api.get('/mall/categories').then((list) => {
      this.setData({ categories: list })
    })
  },

  loadGoods() {
    let url = '/mall/goods?'
    if (this.data.keyword) url += 'keyword=' + this.data.keyword + '&'
    if (this.data.categoryId) url += 'categoryId=' + this.data.categoryId
    api.get(url).then((list) => {
      this.setData({ goods: list })
    })
  },

  // 购物车数量角标
  loadCart() {
    api.get('/mall/cart/' + app.globalData.userId).then((list) => {
      this.setData({ cartCount: list.length })
    })
  },

  // 我的订单各状态数量
  loadOrderStat() {
    api.get('/mall/orders/' + app.globalData.userId).then((orders) => {
      const stat = { unpaid: 0, prepare: 0, shipping: 0, aftersale: 0 }
      orders.forEach((o) => {
        const s = o.order.status
        if (s === 1) stat.unpaid++
        else if (s === 2) stat.prepare++
        else if (s === 3) stat.shipping++
        else if (s === 5) stat.aftersale++
      })
      this.setData({ orderStat: stat })
    })
  },

  onInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  search() {
    this.loadGoods()
  },

  pickCate(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ categoryId: id === '' ? null : Number(id) })
    this.loadGoods()
  },

  explore() {
    wx.showToast({ title: '正在探索本周新品', icon: 'none' })
  },

  goAuth() {
    wx.navigateTo({ url: '/pages/auth/auth' })
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  // 快速加入购物车
  quickAdd(e) {
    const item = e.currentTarget.dataset.item
    const spec = item.spec ? item.spec.split(',')[0] : ''
    api.post('/mall/cart/add', {
      userId: app.globalData.userId,
      goodsId: item.id,
      spec: spec,
      quantity: 1
    }).then(() => {
      wx.showToast({ title: '已加入购物车', icon: 'success' })
      this.loadCart()
    })
  },

  goCart() {
    wx.navigateTo({ url: '/pages/cart/cart' })
  },

  goOrders(e) {
    const status = e.currentTarget.dataset.status
    let url = '/pages/order-list/order-list'
    if (status !== '' && status !== undefined) {
      url += '?status=' + status
    }
    wx.navigateTo({ url })
  }
})
