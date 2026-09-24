import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/recharge',
  },
  {
    path: '/recharge',
    name: 'recharge',
    component: () => import('@/views/recharge/RechargePage.vue'),
  },
  {
    path: '/orders',
    component: () => import('@/views/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'orders',
        component: () => import('@/views/orders/OrdersPage.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
