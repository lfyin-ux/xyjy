// 小程序入口
App({
  globalData: {
    // 后端接口基础地址 真机调试请改为局域网IP
    baseUrl: 'http://localhost:8080/api',
    // 当前登录用户
    userInfo: null,
    // 当前用户ID 演示默认使用mock用户1 林小满
    userId: 1
  },

  onLaunch() {
    // 演示自动登录 使用第一个mock用户
    const openid = 'openid_001'
    wx.request({
      url: this.globalData.baseUrl + '/auth/wxLogin',
      method: 'POST',
      data: { openid },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          this.globalData.userInfo = res.data.data
          this.globalData.userId = res.data.data.id
        }
      }
    })
  }
})
