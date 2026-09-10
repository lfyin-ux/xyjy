const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    tab: 'post',
    allList: [],
    list: [],
    imgBase: ''
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl })
    this.loadList()
  },

  loadList() {
    api.get('/square/myComments/' + app.globalData.userId).then((list) => {
      list.forEach((item) => {
        const post = item.post || {}
        item.postPreview = post.content || ''
        item.postFirstImg = post.images ? post.images.split(',')[0] : ''
        item.timeText = (item.comment.createTime || '').replace('T', ' ').slice(0, 16)
        const vis = Number(item.comment.visibility || 3)
        if (vis === 1) item.visLabel = '发布人可见'
        else if (vis === 2) item.visLabel = '回复人可见'
        else item.visLabel = ''
      })
      this.setData({ allList: list })
      this.filterList()
    })
  },

  switchTab(e) {
    this.setData({ tab: e.currentTarget.dataset.tab })
    this.filterList()
  },

  filterList() {
    const tab = this.data.tab
    const list = this.data.allList.filter((item) => {
      return tab === 'reply' ? item.isReply : !item.isReply
    })
    this.setData({ list })
  },

  openPost(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/post-detail/post-detail?id=' + id })
  }
})
