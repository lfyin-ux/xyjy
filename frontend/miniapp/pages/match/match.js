const api = require('../../utils/api')
const authGate = require('../../utils/authGate')
const { formatPostSub } = require('../../utils/format')
const schoolContext = require('../../utils/schoolContext')
const app = getApp()

Page({
  data: {
    posts: [],
    imgBase: '',
    locked: false,
    viewOnly: false,
    schoolBanner: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setData({ selected: 0 })
    this.refreshSchoolContext()
    this.checkAuth()
  },

  refreshSchoolContext() {
    const uid = app.globalData.userId
    if (!uid) {
      this.setData({ viewOnly: false, schoolBanner: '' })
      return
    }
    schoolContext.load(app).then((ctx) => {
      if (!ctx) return
      let banner = '当前：' + (ctx.currentSchoolName || '所属学校')
      if (ctx.viewOnly) {
        banner += '（浏览模式，商城消费满2000元可互动，已消费¥' + ctx.mallTotalSpent + '）'
      }
      this.setData({ viewOnly: ctx.viewOnly, schoolBanner: banner })
    })
  },

  checkAuth() {
    const userId = app.globalData.userId
    // 游客可以先浏览公开动态，登录只在互动时触发。
    if (!userId) {
      this.setData({ locked: false })
      this.loadPosts()
      return
    }
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
    const userId = app.globalData.userId
    const url = '/square/list?pageNum=1&pageSize=20' + (userId ? '&userId=' + userId : '')
    api.get(url).then((page) => {
      const posts = (page.records || []).map((item) => {
        item.firstImg = item.post.images ? item.post.images.split(',')[0] : ''
        item.liked = false
        item.subText = formatPostSub(item.user && item.user.school, item.post && item.post.place)
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
    if (!app.checkLogin()) return
    if (!schoolContext.ensureWrite(app, '点赞')) return
    const id = e.currentTarget.dataset.id
    const idx = e.currentTarget.dataset.idx
    api.post('/square/like?postId=' + id + '&userId=' + app.globalData.userId).then((res) => {
      const key = 'posts[' + idx + '].post.likeCount'
      const likeKey = 'posts[' + idx + '].liked'
      this.setData({ [key]: res.likeCount, [likeKey]: res.liked })
    })
  },

  goAuth() {
    if (!app.checkLogin()) return
    wx.navigateTo({ url: '/pages/auth/auth' })
  },

  goPublish() {
    if (!app.checkLogin()) return
    if (!schoolContext.ensureWrite(app, '发布动态')) return
    wx.navigateTo({ url: '/pages/post-publish/post-publish' })
  }
})
