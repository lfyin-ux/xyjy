<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">碰个<i>面</i> 后台</div>
      <el-menu :default-active="$route.path" router background-color="#201a3b" text-color="#c3c0d6"
        active-text-color="#fff">
        <el-menu-item index="/dashboard"><el-icon><DataLine /></el-icon><span>数据总览</span></el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-sub-menu index="audit">
          <template #title><el-icon><Stamp /></el-icon><span>认证审核</span></template>
          <el-menu-item index="/personal-audit">个人认证审核</el-menu-item>
          <el-menu-item index="/school-audit">学校认证审核</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="content">
          <template #title><el-icon><ChatDotSquare /></el-icon><span>内容管理</span></template>
          <el-menu-item index="/content-audit">内容审核</el-menu-item>
          <el-menu-item index="/reports">举报处理</el-menu-item>
          <el-menu-item index="/filter-words">词库管理</el-menu-item>
          <el-menu-item index="/hit-log">命中记录</el-menu-item>
          <el-menu-item index="/square">校园广场管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="life">
          <template #title><el-icon><Van /></el-icon><span>校园生活</span></template>
          <el-menu-item index="/errand">跑腿订单管理</el-menu-item>
          <el-menu-item index="/second">二手市场管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="mall">
          <template #title><el-icon><Goods /></el-icon><span>商城管理</span></template>
          <el-menu-item index="/goods">商品管理</el-menu-item>
          <el-menu-item index="/orders">商城订单管理</el-menu-item>
          <el-menu-item index="/refund">退款管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admins"><el-icon><Setting /></el-icon><span>管理员管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="page-name">{{ $route.name }}</span>
        <el-dropdown @command="handleCommand">
          <span class="admin-info">
            <el-icon><Avatar /></el-icon>
            {{ adminName }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const adminName = computed(() => {
  const a = sessionStorage.getItem('admin')
  return a ? JSON.parse(a).nickname || JSON.parse(a).username : '管理员'
})

// 顶部下拉操作
const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    sessionStorage.removeItem('admin')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #201a3b;
  overflow-y: auto;
}

.aside :deep(.el-menu) {
  border-right: none;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: 800;
}

.logo i {
  color: #d8ff7b;
  font-style: normal;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}

.page-name {
  font-size: 18px;
  font-weight: 600;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
}

.main {
  background: #f0f2f5;
}
</style>
