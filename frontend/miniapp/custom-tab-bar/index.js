Component({
  data: {
    selected: 0,
    color: '#bbb9c4',
    selectedColor: '#171527',
    list: [
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
  },

  methods: {
    switchTab(e) {
      const index = Number(e.currentTarget.dataset.index)
      const item = this.data.list[index]
      if (!item || index === this.data.selected) return
      this.setData({ selected: index })
      wx.switchTab({ url: item.pagePath })
    }
  }
})
