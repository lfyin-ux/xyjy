import { createRouter, createWebHashHistory } from 'vue-router'

// 路由配置
const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: '数据总览', component: () => import('../views/Dashboard.vue') },
      { path: 'users', name: '用户管理', component: () => import('../views/Users.vue') },
      { path: 'school-audit', name: '学校认证审核', component: () => import('../views/SchoolAudit.vue') },
      { path: 'content-audit', name: '内容审核', component: () => import('../views/ContentAudit.vue') },
      { path: 'reports', name: '举报处理', component: () => import('../views/Reports.vue') },
      { path: 'feedback', name: '意见反馈', component: () => import('../views/Feedback.vue') },
      { path: 'filter-words', name: '词库管理', component: () => import('../views/FilterWords.vue') },
      { path: 'hit-log', name: '命中记录', component: () => import('../views/HitLog.vue') },
      { path: 'square', name: '校园广场管理', component: () => import('../views/Square.vue') },
      { path: 'errand', name: '跑腿订单管理', component: () => import('../views/Errand.vue') },
      { path: 'second', name: '二手市场管理', component: () => import('../views/Second.vue') },
      { path: 'goods', name: '商品管理', component: () => import('../views/Goods.vue') },
      { path: 'orders', name: '商城订单管理', component: () => import('../views/Orders.vue') },
      { path: 'refund', name: '退款管理', component: () => import('../views/Refund.vue') },
      { path: 'admins', name: '管理员管理', component: () => import('../views/Admins.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 简单登录拦截
router.beforeEach((to, from, next) => {
  const admin = sessionStorage.getItem('admin')
  if (to.path !== '/login' && !admin) {
    next('/login')
  } else {
    next()
  }
})

export default router
