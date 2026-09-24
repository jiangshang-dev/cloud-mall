import request from './request'

export const listProducts = () => request.get('/mall/admin/product/list')
export const saveProduct = (data) => request.post('/mall/admin/product/save', data)
export const batchCdk = (data) => request.post('/mall/admin/cdk/batch', data)
export const listCdk = (params) => request.get('/mall/admin/cdk/list', { params })
export const listDelivery = (params) => request.get('/mall/admin/delivery/list', { params })
