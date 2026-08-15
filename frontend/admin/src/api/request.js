import axios from 'axios'
import { ElMessage } from 'element-plus'

// axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 20000
})

// 响应拦截 统一处理返回结构
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
