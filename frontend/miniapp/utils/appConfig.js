const api = require('./api')

const TAB_LIST = [
  {
    pagePath: '/pages/match/match',
    text: '广场',
    iconPath: '/images/tabbar/match_solid.png',
    selectedIconPath: '/images/tabbar/match_on_solid.png'
  },
  {
    pagePath: '/pages/life/life',
    text: '生活',
    iconPath: '/images/tabbar/life_solid.png',
    selectedIconPath: '/images/tabbar/life_on_solid.png'
  },
  {
    pagePath: '/pages/mall/mall',
    text: '商城',
    iconPath: '/images/tabbar/mall_solid.png',
    selectedIconPath: '/images/tabbar/mall_on_solid.png'
  },
  {
    pagePath: '/pages/mine/mine',
    text: '我的',
    iconPath: '/images/tabbar/mine_solid.png',
    selectedIconPath: '/images/tabbar/mine_on_solid.png'
  }
]

function applyMallEnabled(app, mallEnabled) {
  app.globalData.mallEnabled = mallEnabled
  wx.setStorageSync('appConfig', { mallEnabled })
  refreshTabBar(app)
  refreshMinePageMallFlag(mallEnabled)
}

function refreshMinePageMallFlag(mallEnabled) {
  const pages = getCurrentPages()
  if (!pages.length) return
  const page = pages[pages.length - 1]
  if (!page || page.route !== 'pages/mine/mine') return
  if (typeof page.setData === 'function') {
    page.setData({ mallEnabled })
  }
}

function load(app) {
  return api.get('/common/app-config').then((cfg) => {
    const mallEnabled = !!(cfg && cfg.mallEnabled)
    applyMallEnabled(app, mallEnabled)
    return cfg
  }).catch(() => {
    const mallEnabled = isMallEnabled(app)
    refreshTabBar(app)
    refreshMinePageMallFlag(mallEnabled)
    return { mallEnabled }
  })
}

function isMallEnabled(app) {
  if (app && app.globalData && app.globalData.mallEnabled != null) {
    return app.globalData.mallEnabled
  }
  const cached = wx.getStorageSync('appConfig')
  if (cached && cached.mallEnabled != null) {
    return !!cached.mallEnabled
  }
  return false
}

function tabListForApp(app) {
  if (isMallEnabled(app)) return TAB_LIST
  return TAB_LIST.filter((t) => t.pagePath !== '/pages/mall/mall')
}

function refreshTabBar(app) {
  const pages = getCurrentPages()
  if (!pages.length) return
  const page = pages[pages.length - 1]
  if (page && typeof page.getTabBar === 'function' && page.getTabBar()) {
    page.getTabBar().updateTabList()
  }
}

function setTabSelected(page) {
  if (!page || typeof page.getTabBar !== 'function') return
  const tabBar = page.getTabBar()
  if (!tabBar || typeof tabBar.setSelected !== 'function') return
  const route = page.route ? '/' + page.route : ''
  tabBar.setSelected(route)
}

function blockMallPages() {
  const app = getApp()
  if (!isMallEnabled(app)) {
    wx.switchTab({ url: '/pages/match/match' })
    return true
  }
  return false
}

module.exports = {
  load,
  isMallEnabled,
  tabListForApp,
  refreshTabBar,
  setTabSelected,
  blockMallPages,
  TAB_LIST
}
