const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    mode: 'detail',
    post: {},
    comments: [],
    commentText: '',
    myPosts: [],
    postId: null,
    imgBase: '',
    likeUsers: []
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl })
    if (options.my) {
      this.setData({ mode: 'my' })
      wx.setNavigationBarTitle({ title: '我的动态' })
      this.loadMyPosts()
    } else if (options.id) {
      this.setData({ postId: options.id })
      this.loadDetail(options.id)
      this.loadComments(options.id)
      this.loadLikeUsers(options.id)
    }
  },

  loadDetail(id) {
    api.get('/square/detail/' + id).then((vo) => {
      vo.firstImg = vo.post.images ? vo.post.images.split(',')[0] : ''
      this.setData({ post: vo })
    })
  },

  loadComments(id) {
    api.get('/square/comments/' + id).then((list) => {
      this.setData({ comments: list })
    })
  },

  loadLikeUsers(id) {
    api.get('/square/likeUsers/' + id).then((list) => {
      this.setData({ likeUsers: list })
    })
  },

  viewLikeUser(e) {
    wx.navigateTo({ url: '/pages/profile/profile?id=' + e.currentTarget.dataset.id })
  },

  loadMyPosts() {
    api.get('/square/my/' + app.globalData.userId).then((list) => {
      list.forEach((item) => {
        item.firstImg = item.images ? item.images.split(',')[0] : ''
      })
      this.setData({ myPosts: list })
    })
  },

  onComment(e) {
    this.setData({ commentText: e.detail.value })
  },

  submitComment() {
    if (!this.data.commentText) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' })
      return
    }
    api.post('/square/comment', {
      postId: Number(this.data.postId),
      userId: app.globalData.userId,
      content: this.data.commentText
    }).then((tip) => {
      wx.showToast({ title: tip || '评论已发布', icon: 'none' })
      this.setData({ commentText: '' })
      this.loadComments(this.data.postId)
    })
  },

  delPost(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定删除这条动态吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/square/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' })
            this.loadMyPosts()
          })
        }
      }
    })
  },

  viewPost(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ mode: 'detail', postId: id })
    this.loadDetail(id)
    this.loadComments(id)
    this.loadLikeUsers(id)
  }
})
