const api = require('../../utils/api')
const authGate = require('../../utils/authGate')
const app = getApp()

Page({
  data: {
    posts: [],
    imgBase: '',
    locked: false
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.checkAuth()
  },

  checkAuth() {
    const userId = app.globalData.userId
    authGate.checkFullAccess(userId, () => {
      this.setData({ locked: false })
      this.loadPosts()
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
        this.loadPosts()
      } else {
        this.setData({ locked: true })
      }
    }).catch(() => {
      this.setData({ locked: true })
    })
    */
  },

  loadPosts() {
    api.get('/square/list?pageNum=1&pageSize=20&userId=' + app.globalData.userId).then((page) => {
      const posts = (page.records || []).map((item) => {
        item.firstImg = item.post.images ? item.post.images.split(',')[0] : ''
        item.liked = false
        return item
      })
      this.setData({ posts })
    })
  },

  viewProfile(e) {
    wx.navigateTo({ url: '/pages/profile/profile?id=' + e.currentTarget.dataset.id })
  },

  openPost(e) {
    wx.navigateTo({ url: '/pages/post-detail/post-detail?id=' + e.currentTarget.dataset.id })
  },

  toggleLike(e) {
    const id = e.currentTarget.dataset.id
    const idx = e.currentTarget.dataset.idx
    api.post('/square/like?postId=' + id + '&userId=' + app.globalData.userId).then((res) => {
      const key = 'posts[' + idx + '].post.likeCount'
      const likeKey = 'posts[' + idx + '].liked'
      this.setData({ [key]: res.likeCount, [likeKey]: res.liked })
    })
  },

  goAuth() {
    wx.navigateTo({ url: '/pages/auth/auth' })
  },

  goPublish() {
    wx.navigateTo({ url: '/pages/post-publish/post-publish' })
  }
})
