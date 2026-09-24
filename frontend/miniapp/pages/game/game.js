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
    showJoin: false,
    joinTeamId: null,
    joinContact: '',
    showDetail: false,
    members: [],
    gameName: '',
    playTime: '',
    needNum: '',
    requireDesc: '',
    creatorContact: '',
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
    const uid = app.globalData.userId
    const url = '/game/list' + (uid ? '?userId=' + uid : '')
    api.get(url).then((list) => this.setData({ allList: list }))
    if (!app.globalData.userId) {
      this.setData({ createdList: [], joinedList: [] })
      this.updateList()
      return
    }
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
    const uid = app.globalData.userId
    const url = '/game/members/' + id + (uid ? '?userId=' + uid : '')
    api.get(url).then((list) => {
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
    if (!app.checkLogin()) return
    const id = e.currentTarget.dataset.id
    this.setData({ showJoin: true, joinTeamId: id, joinContact: '' })
  },

  closeJoin() {
    this.setData({ showJoin: false, joinTeamId: null, joinContact: '' })
  },

  onJoinContact(e) {
    this.setData({ joinContact: e.detail.value })
  },

  confirmJoin() {
    if (!app.checkLogin()) return
    const contact = (this.data.joinContact || '').trim()
    if (!contact) {
      wx.showToast({ title: '请填写联系方式', icon: 'none' })
      return
    }
    const id = this.data.joinTeamId
    api.post('/game/join?teamId=' + id + '&userId=' + app.globalData.userId + '&contact=' + encodeURIComponent(contact)).then(() => {
      wx.showToast({ title: '加入成功', icon: 'success' })
      this.setData({ showJoin: false, joinTeamId: null, joinContact: '' })
      this.loadAll()
    })
  },

  copyContact(e) {
    const contact = e.currentTarget.dataset.contact
    if (!contact) return
    wx.setClipboardData({
      data: contact,
      success: () => wx.showToast({ title: '已复制', icon: 'success' })
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
    if (!app.checkLogin()) return
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
  onCreatorContact(e) { this.setData({ creatorContact: e.detail.value }) },

  create() {
    if (!app.checkLogin()) return
    const d = this.data
    if (!d.gameName || !d.needNum) {
      wx.showToast({ title: '请填写游戏名称和人数', icon: 'none' })
      return
    }
    if (!d.creatorContact || !d.creatorContact.trim()) {
      wx.showToast({ title: '请填写联系方式', icon: 'none' })
      return
    }
    api.post('/game/create', {
      creatorId: app.globalData.userId,
      gameName: d.gameName,
      playTime: d.playTime,
      needNum: Number(d.needNum),
      requireDesc: d.requireDesc,
      creatorContact: d.creatorContact.trim()
    }).then(() => {
      wx.showToast({ title: '组局已发起', icon: 'success' })
      this.setData({
        showCreate: false,
        gameName: '',
        playTime: '',
        needNum: '',
        requireDesc: '',
        creatorContact: ''
      })
      this.loadAll()
    })
  }
})
