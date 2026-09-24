import axios from 'axios'
import { message } from 'ant-design-vue'

const request = axios.create({
  baseURL: '/jeecg-boot',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('X-Access-Token')
  if (token) {
    config.headers['X-Access-Token'] = token
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && typeof data.success === 'boolean') {
      if (!data.success) {
        message.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }
      return data
    }
    return data
  },
  (err) => {
    message.error(err?.response?.data?.message || err.message || '网络错误')
    return Promise.reject(err)
  },
)

export default request
