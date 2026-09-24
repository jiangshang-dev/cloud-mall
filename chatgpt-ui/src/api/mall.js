import request from './request'

export const listProducts = () => request.get('/mall/chatgpt/product/list')
export const createOrder = (productId) => request.post('/mall/chatgpt/order/create', { productId })
export const listOrders = (params) => request.get('/mall/chatgpt/order/list', { params })
export const orderDetail = (orderNo) => request.get(`/mall/chatgpt/order/${orderNo}`)
export const applyRefund = (orderNo) => request.post(`/mall/chatgpt/order/${orderNo}/refund`)
