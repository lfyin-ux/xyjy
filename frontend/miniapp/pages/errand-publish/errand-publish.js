const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    title: '',
    fromPlace: '',
    toPlace: '',
    finishTime: '',
    fee: '',
    content: '',
    contact: ''
  },

  onTitle(e) { this.setData({ title: e.detail.value }) },
  onFrom(e) { this.setData({ fromPlace: e.detail.value }) },
  onTo(e) { this.setData({ toPlace: e.detail.value }) },
  onTime(e) { this.setData({ finishTime: e.detail.value }) },
  onFee(e) { this.setData({ fee: e.detail.value }) },
  onContent(e) { this.setData({ content: e.detail.value }) },
  onContact(e) { this.setData({ contact: e.detail.value }) },

  publish() {
    const d = this.data
    if (!d.title || !d.fromPlace || !d.toPlace || !d.fee || Number(d.fee) <= 0) {
      wx.showToast({ title: '请完整填写发单信息', icon: 'none' })
      return
    }
    if (!d.contact) {
      wx.showToast({ title: '请填写联系方式', icon: 'none' })
      return
    }
    api.post('/errand/publish', {
      publisherId: app.globalData.userId,
      title: d.title,
      fromPlace: d.fromPlace,
      toPlace: d.toPlace,
      finishTime: d.finishTime,
      fee: Number(d.fee),
      content: d.content,
      publisherContact: d.contact
    }).then(() => {
      wx.showToast({ title: '订单已发布', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 700)
    })
  }
})
