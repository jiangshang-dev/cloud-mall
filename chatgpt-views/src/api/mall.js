import request from './request'

export function getLoginStatus() {
  return request.get('/mall/chatgpt/login/status')
}

export function verifyCdk(code) {
  return request.post('/mall/chatgpt/cdk/verify', { code })
}

export function redeemCdk(code) {
  return request.post('/mall/chatgpt/order/redeem', { code })
}

export function getDeliveryStatus(orderNo) {
  return request.get(`/mall/chatgpt/delivery/status/${orderNo}`)
}

export function getOrderList(params) {
  return request.get('/mall/chatgpt/order/list', { params })
}

export function getOrderDetail(orderNo) {
  return request.get(`/mall/chatgpt/order/${orderNo}`)
}

export function applyRefund(orderNo) {
  return request.post(`/mall/chatgpt/order/${orderNo}/refund`)
}
