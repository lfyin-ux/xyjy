const api = require('../../utils/api')
const app = getApp()

const VIS_OPTIONS = [
  { value: 3, label: '全部可见' },
  { value: 1, label: '发布人可见' },
  { value: 2, label: '回复人可见' }
]

Page({
  data: {
    mode: 'detail',
    post: {},
    comments: [],
    commentText: '',
    myPosts: [],
    postId: null,
    imgBase: '',
    likeUsers: [],
    visibility: 3,
    visibilityLabel: '全部可见',
    visOptions: VIS_OPTIONS,
    replyToUserId: null,
    replyToNickname: '',
    parentId: null,
    myUserId: null
  },

  onLoad(options) {
    this.setData({ imgBase: app.globalData.baseUrl, myUserId: app.globalData.userId })
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
    const userId = app.globalData.userId
    const url = '/square/comments/' + id + (userId ? '?userId=' + userId : '')
    api.get(url).then((list) => {
      const myId = Number(userId)
      list.forEach((item) => {
        const c = item.comment || {}
        const authorId = Number(item.user && item.user.id)
        const replyToId = c.replyToUserId
          ? Number(c.replyToUserId)
          : (item.replyToUser ? Number(item.replyToUser.id) : null)
        const vis = Number(c.visibility || 3)
        item.isMine = authorId === myId
        item.repliedToMe = replyToId === myId && authorId !== myId
        item.myReply = item.isMine && !!replyToId
        item.isPrivate = vis === 1 || vis === 2
        item.replyName = item.replyToUser ? item.replyToUser.nickname : ''
        if (item.isMine && vis === 2 && item.replyName) {
          item.privateHint = '仅 @' + item.replyName + ' 可见'
        } else if (item.isMine && vis === 1) {
          item.privateHint = '仅发布人可见'
        } else if (item.repliedToMe && vis === 2) {
          item.privateHint = '仅你可见的回复'
        } else if (item.isPrivate) {
          item.privateHint = vis === 2 ? '回复人可见' : '发布人可见'
        }
      })
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

  pickVisibility() {
    const labels = VIS_OPTIONS.map((o) => o.label)
    wx.showActionSheet({
      itemList: labels,
      success: (res) => {
        const opt = VIS_OPTIONS[res.tapIndex]
        if (opt.value === 2 && !this.data.replyToUserId) {
          wx.showToast({ title: '请先点击要回复的评论', icon: 'none' })
          return
        }
        this.setData({ visibility: opt.value, visibilityLabel: opt.label })
      }
    })
  },

  replyComment(e) {
    if (!app.checkLogin()) return
    const { userid, nickname, commentid } = e.currentTarget.dataset
    if (Number(userid) === Number(app.globalData.userId)) {
      wx.showToast({ title: '不能回复自己的评论', icon: 'none' })
      return
    }
    this.setData({
      replyToUserId: userid,
      replyToNickname: nickname,
      parentId: commentid,
      visibility: 2,
      visibilityLabel: '回复人可见'
    })
  },

  cancelReply() {
    this.setData({
      replyToUserId: null,
      replyToNickname: '',
      parentId: null,
      visibility: 3,
      visibilityLabel: '全部可见'
    })
  },

  submitComment() {
    if (!app.checkLogin()) return
    if (!this.data.commentText) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' })
      return
    }
    if (this.data.visibility === 2 && !this.data.replyToUserId) {
      wx.showToast({ title: '回复人可见需先点击要回复的评论', icon: 'none' })
      return
    }
    if (this.data.replyToUserId && Number(this.data.replyToUserId) === Number(app.globalData.userId)) {
      wx.showToast({ title: '不能回复自己', icon: 'none' })
      return
    }
    const payload = {
      postId: Number(this.data.postId),
      userId: app.globalData.userId,
      content: this.data.commentText,
      visibility: this.data.visibility
    }
    if (this.data.replyToUserId) {
      payload.replyToUserId = Number(this.data.replyToUserId)
    }
    if (this.data.parentId) {
      payload.parentId = Number(this.data.parentId)
    }
    api.post('/square/comment', payload).then((tip) => {
      wx.showToast({ title: tip || '评论已发布', icon: 'none' })
      this.setData({
        commentText: '',
        replyToUserId: null,
        replyToNickname: '',
        parentId: null,
        visibility: 3,
        visibilityLabel: '全部可见'
      })
      this.loadComments(this.data.postId)
    })
  },

  delComment(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定删除这条评论吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/square/comment/' + id + '?userId=' + app.globalData.userId).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' })
            this.loadComments(this.data.postId)
            this.loadDetail(this.data.postId)
          })
        }
      }
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
