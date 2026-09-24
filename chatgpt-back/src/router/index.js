import { createRouter, createWebHistory } from 'vue-router'

export default createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/products' },
        { path: 'products', component: () => import('@/views/ProductPage.vue') },
        { path: 'cdks', component: () => import('@/views/CdkPage.vue') },
        { path: 'delivery', component: () => import('@/views/DeliveryPage.vue') },
      ],
    },
  ],
})
