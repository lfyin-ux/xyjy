const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    categories: [],
    goods: [],
    keyword: '',
    categoryId: null,
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadCategories()
  },

  onShow() {
    this.loadGoods()
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

  goDetail(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  goCart() {
    wx.navigateTo({ url: '/pages/cart/cart' })
  }
})
