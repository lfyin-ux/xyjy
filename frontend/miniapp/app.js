// 小程序入口
App({
  globalData: {
    // 后端接口基础地址 真机调试请改为局域网IP
    baseUrl: 'https://tongxingshikong.cn/api',
    // 当前登录用户
    userInfo: null,
    // 当前用户ID
    userId: null
  },

  onLaunch() {
    const userInfo = wx.getStorageSync('userInfo')
    const userId = wx.getStorageSync('userId')
    if (userInfo && userId) {
      this.globalData.userInfo = userInfo
      this.globalData.userId = Number(userId)
    }
  },

  /**
   * 检查是否已登录 未登录跳转登录页
   */
  checkLogin() {
    if (!this.globalData.userId) {
      wx.navigateTo({ url: '/pages/login/login' })
      return false
    }
    return true
  }
})
