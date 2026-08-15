const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    teams: [],
    showCreate: false,
    gameName: '',
    playTime: '',
    needNum: '',
    requireDesc: ''
  },

  onShow() {
    this.load()
  },

  load() {
    api.get('/game/list').then((list) => this.setData({ teams: list }))
  },

  join(e) {
    const id = e.currentTarget.dataset.id
    api.post('/game/join?teamId=' + id + '&userId=' + app.globalData.userId).then(() => {
      wx.showToast({ title: '加入成功', icon: 'success' })
      this.load()
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
      this.load()
    })
  }
})
