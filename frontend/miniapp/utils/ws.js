// WebSocket 连接管理 即时通讯
let socketTask = null
let isConnected = false
let reconnectTimer = null
let reconnectCount = 0
const MAX_RECONNECT = 5
let messageListeners = []

// WebSocket服务地址
function getWsUrl() {
  const app = getApp()
  if (!app || !app.globalData) return null
  const baseUrl = app.globalData.baseUrl
  const userId = app.globalData.userId
  if (!userId) return null
  return baseUrl.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws/chat/' + userId
}

// 建立连接
function connect() {
  if (isConnected && socketTask) return
  // 防止重复创建
  if (socketTask) {
    try { socketTask.close({}) } catch (e) {}
    socketTask = null
  }
  const url = getWsUrl()
  if (!url) return

  console.log('[WS] 连接:', url)

  try {
    socketTask = wx.connectSocket({
      url: url,
      header: { 'content-type': 'application/json' },
      fail: function (err) {
        console.error('[WS] connectSocket调用失败', err)
        socketTask = null
      }
    })
  } catch (e) {
    console.error('[WS] connectSocket异常', e)
    socketTask = null
    return
  }

  if (!socketTask) return

  socketTask.onOpen(function () {
    console.log('[WS] 连接成功')
    isConnected = true
    reconnectCount = 0
  })

  socketTask.onMessage(function (res) {
    try {
      const data = JSON.parse(res.data)
      messageListeners.forEach(function (cb) { cb(data) })
    } catch (e) {}
  })

  socketTask.onClose(function () {
    console.log('[WS] 连接关闭')
    isConnected = false
    socketTask = null
    scheduleReconnect()
  })

  socketTask.onError(function (err) {
    console.error('[WS] 连接失败，请确认后端已启动且已勾选不校验合法域名')
    isConnected = false
    socketTask = null
    scheduleReconnect()
  })
}

// 定时重连 有次数限制 避免无限重试
function scheduleReconnect() {
  if (reconnectTimer) return
  if (reconnectCount >= MAX_RECONNECT) {
    console.log('[WS] 达到最大重连次数，停止重连')
    return
  }
  reconnectCount++
  // 重连间隔递增 3s 6s 9s 12s 15s
  var delay = reconnectCount * 3000
  console.log('[WS] ' + delay / 1000 + '秒后重连 (第' + reconnectCount + '次)')
  reconnectTimer = setTimeout(function () {
    reconnectTimer = null
    connect()
  }, delay)
}

// 关闭连接
function close() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  reconnectCount = MAX_RECONNECT // 阻止后续重连
  if (socketTask) {
    try { socketTask.close({}) } catch (e) {}
    socketTask = null
  }
  isConnected = false
  messageListeners = []
}

// 发送消息
function send(data) {
  if (socketTask && isConnected) {
    socketTask.send({ data: JSON.stringify(data) })
  }
}

// 添加消息监听器 返回移除函数
function addListener(callback) {
  messageListeners.push(callback)
  return function () {
    var idx = messageListeners.indexOf(callback)
    if (idx >= 0) messageListeners.splice(idx, 1)
  }
}

// 重置重连计数 手动触发重连
function resetReconnect() {
  reconnectCount = 0
}

function connected() {
  return isConnected
}

module.exports = {
  connect: connect,
  close: close,
  send: send,
  addListener: addListener,
  connected: connected,
  resetReconnect: resetReconnect
}
