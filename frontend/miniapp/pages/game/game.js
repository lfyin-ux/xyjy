const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    tab: 'all',
    allList: [],
    createdList: [],
    joinedList: [],
    currentList: [],
    showCreate: false,
    showDetail: false,
    members: [],
    gameName: '',
    playTime: '',
    needNum: '',
    requireDesc: '',
    imgBase: '',
    myId: 0
  },

  onLoad() {
    this.setData({ imgBase: app.globalData.baseUrl, myId: Number(app.globalData.userId) })
  },

  onShow() {
    this.loadAll()
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.t
    this.setData({ tab })
    this.updateList()
  },

  loadAll() {
    api.get('/game/list').then((list) => this.setData({ allList: list }))
    api.get('/game/myCreated/' + app.globalData.userId).then((list) => this.setData({ createdList: list }))
    api.get('/game/myJoined/' + app.globalData.userId).then((list) => {
      this.setData({ joinedList: list })
      this.updateList()
    })
  },

  updateList() {
    const tab = this.data.tab
    if (tab === 'all') this.setData({ currentList: this.data.allList })
    else if (tab === 'created') this.setData({ currentList: this.data.createdList })
    else this.setData({ currentList: this.data.joinedList })
  },

  // 查看组局成员详情
  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    api.get('/game/members/' + id).then((list) => {
      this.setData({ members: list, showDetail: true })
    })
  },

  closeDetail() {
    this.setData({ showDetail: false })
  },

  viewProfile(e) {
    wx.navigateTo({ url: '/pages/profile/profile?id=' + e.currentTarget.dataset.id })
  },

  join(e) {
    const id = e.currentTarget.dataset.id
    api.post('/game/join?teamId=' + id + '&userId=' + app.globalData.userId).then(() => {
      wx.showToast({ title: '加入成功', icon: 'success' })
      this.loadAll()
    })
  },

  quit(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定退出该组局吗？',
      success: (res) => {
        if (res.confirm) {
          api.post('/game/quit?teamId=' + id + '&userId=' + app.globalData.userId).then(() => {
            wx.showToast({ title: '已退出', icon: 'success' })
            this.loadAll()
          })
        }
      }
    })
  },

  openCreate() {
    this.setData({ showCreate: true })
  },

  closeCreate() {
    this.setData({ showCreate: false })
  },

  noop() {},

  onName(e) { this.setData({ gameName: e.detail.value }) },
  onTime(e) { this.setData({ playTime: e.detail.value }) },
  onNum(e) { this.setData({ needNum: e.detail.value }) },
  onReq(e) { this.setData({ requireDesc: e.detail.value }) },

  create() {
    const d = this.data
    if (!d.gameName || !d.needNum) {
      wx.showToast({ title: '请填写游戏名称和人数', icon: 'none' })
      return
    }
    api.post('/game/create', {
      creatorId: app.globalData.userId,
      gameName: d.gameName,
      playTime: d.playTime,
      needNum: Number(d.needNum),
      requireDesc: d.requireDesc
    }).then(() => {
      wx.showToast({ title: '组局已发起', icon: 'success' })
      this.setData({ showCreate: false, gameName: '', playTime: '', needNum: '', requireDesc: '' })
      this.loadAll()
    })
  }
})
