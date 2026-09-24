const api = require('../../utils/api')
const violationNotice = require('../../utils/violationNotice')
const app = getApp()

Page({
  data: {
    list: []
  },

  onShow() {
    const userId = app.globalData.userId
    if (!userId) {
      this.setData({ list: [] })
      return
    }
    api.get('/user/violations/' + userId).then((list) => {
      const rows = (list || []).map((item) => ({
        ...item,
        typeText: violationNotice.typeText(item.type),
        timeText: violationNotice.formatTime(item.createTime)
      }))
      this.setData({ list: rows })
      api.post('/user/violations/read-all?userId=' + userId).catch(() => {})
    })
  }
})
