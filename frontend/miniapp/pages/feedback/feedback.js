const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    content: '',
    contact: ''
  },

  onContent(e) {
    this.setData({ content: e.detail.value })
  },

  onContact(e) {
    this.setData({ contact: e.detail.value })
  },

  submit() {
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请输入反馈内容', icon: 'none' })
      return
    }
    api.post('/user/feedback', {
      userId: app.globalData.userId,
      content: this.data.content,
      contact: this.data.contact
    }).then(() => {
      wx.showToast({ title: '感谢反馈', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 800)
    })
  }
})
