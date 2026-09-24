import { createRouter, createWebHistory } from 'vue-router'

export default createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/layout/MainLayout.vue'),
      children: [
        { path: '', redirect: '/shop' },
        { path: 'shop', name: 'shop', component: () => import('@/views/shop/ShopPage.vue') },
        { path: 'orders', name: 'orders', component: () => import('@/views/orders/OrdersPage.vue') },
      ],
    },
  ],
})
