const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    tab: 'recommend',
    recommendList: [],
    current: null,
    posts: [],
    filterGender: null,
    imgBase: ''
  },

  onLoad() {
    // 图片基础地址
    this.setData({ imgBase: app.globalData.baseUrl })
  },

  onShow() {
    this.loadRecommend()
    this.loadPosts()
  },

  switchTab(e) {
    this.setData({ tab: e.currentTarget.dataset.tab })
  },

  // 加载推荐列表
  loadRecommend() {
    let url = '/match/recommend?userId=' + app.globalData.userId
    if (this.data.filterGender) {
      url += '&gender=' + this.data.filterGender
    }
    api.get(url).then((list) => {
      list.forEach((u) => {
        u.tagList = u.tags ? u.tags.split(',') : []
      })
      this.setData({
        recommendList: list,
        current: list.length ? list[0] : null
      })
    })
  },

  // 加载广场动态
  loadPosts() {
    api.get('/square/list?pageNum=1&pageSize=20').then((page) => {
      const posts = (page.records || []).map((item) => {
        item.firstImg = item.post.images ? item.post.images.split(',')[0] : ''
        item.liked = false
        return item
      })
      this.setData({ posts })
    })
  },

  setGender(e) {
    const g = Number(e.currentTarget.dataset.g)
    this.setData({ filterGender: this.data.filterGender === g ? null : g })
    this.loadRecommend()
  },

  resetFilter() {
    this.setData({ filterGender: null })
    this.loadRecommend()
  },

  openFilter() {
    wx.showToast({ title: '可按性别筛选', icon: 'none' })
  },

  // 下一张卡片
  nextCard() {
    const list = this.data.recommendList.slice(1)
    this.setData({ recommendList: list, current: list.length ? list[0] : null })
  },

  doSkip() {
    if (!this.data.current) return
    api.post('/match/skip?userId=' + app.globalData.userId + '&targetId=' + this.data.current.id).then(() => {
      wx.showToast({ title: '已跳过', icon: 'none' })
      this.nextCard()
    })
  },

  doLike() {
    if (!this.data.current) return
    api.post('/match/like?userId=' + app.globalData.userId + '&targetId=' + this.data.current.id).then((res) => {
      if (res.matched) {
        wx.showModal({ title: '匹配成功 💜', content: '你们互相喜欢，快去聊天吧！', showCancel: false })
      } else {
        wx.showToast({ title: '心动已送达 💜', icon: 'none' })
      }
      this.nextCard()
    })
  },

  doStar() {
    if (!this.data.current) return
    api.post('/match/like?userId=' + app.globalData.userId + '&targetId=' + this.data.current.id + '&type=2').then(() => {
      wx.showToast({ title: '已加入特别关注', icon: 'none' })
      this.nextCard()
    })
  },

  viewProfile(e) {
    wx.navigateTo({ url: '/pages/profile/profile?id=' + e.currentTarget.dataset.id })
  },

  openPost(e) {
    wx.navigateTo({ url: '/pages/post-detail/post-detail?id=' + e.currentTarget.dataset.id })
  },

  // 广场点赞
  toggleLike(e) {
    const id = e.currentTarget.dataset.id
    const idx = e.currentTarget.dataset.idx
    api.post('/square/like?postId=' + id + '&userId=' + app.globalData.userId).then((res) => {
      const key = 'posts[' + idx + '].post.likeCount'
      const likeKey = 'posts[' + idx + '].liked'
      this.setData({ [key]: res.likeCount, [likeKey]: res.liked })
    })
  },

  goChat() {
    wx.switchTab({ url: '/pages/chat/chat' })
  },

  goPublish() {
    wx.navigateTo({ url: '/pages/post-publish/post-publish' })
  }
})
