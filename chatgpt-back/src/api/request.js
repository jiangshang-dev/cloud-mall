import axios from 'axios'
import { message } from 'ant-design-vue'

const request = axios.create({ baseURL: '/jeecg-boot', timeout: 15000 })
request.interceptors.request.use((c) => {
  const t = localStorage.getItem('X-Access-Token')
  if (t) c.headers['X-Access-Token'] = t
  return c
})
request.interceptors.response.use(
  (res) => {
    const d = res.data
    if (d && typeof d.success === 'boolean') {
      if (!d.success) {
        message.error(d.message || '请求失败')
        return Promise.reject(new Error(d.message || '请求失败'))
      }
      return d
    }
    return d
  },
  (err) => {
    message.error(err?.response?.data?.message || err.message || '网络错误')
    return Promise.reject(err)
  },
)
export default request
