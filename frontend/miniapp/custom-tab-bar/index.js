const appConfig = require('../utils/appConfig')

Component({
  data: {
    selected: 0,
    color: '#bbb9c4',
    selectedColor: '#171527',
    list: []
  },

  lifetimes: {
    attached() {
      this.updateTabList()
    }
  },

  pageLifetimes: {
    show() {
      this.updateTabList()
    }
  },

  methods: {
    updateTabList() {
      const app = getApp()
      const list = appConfig.tabListForApp(app)
      const route = this._currentRoute()
      let selected = list.findIndex((item) => item.pagePath === route)
      if (selected < 0) selected = 0
      this.setData({ list, selected })
    },

    setSelected(pagePath) {
      const idx = this.data.list.findIndex((item) => item.pagePath === pagePath)
      if (idx >= 0) this.setData({ selected: idx })
    },

    _currentRoute() {
      const pages = getCurrentPages()
      if (!pages.length) return ''
      const route = pages[pages.length - 1].route
      return route ? '/' + route : ''
    },

    switchTab(e) {
      const index = Number(e.currentTarget.dataset.index)
      const item = this.data.list[index]
      if (!item || index === this.data.selected) return
      this.setData({ selected: index })
      wx.switchTab({ url: item.pagePath })
    }
  }
})
