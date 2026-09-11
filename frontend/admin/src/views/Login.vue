<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-title">
        <span>同行<i>时空</i></span>
        <p>校园社交平台管理后台</p>
      </div>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="请输入账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" placeholder="请输入密码"
            :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminLogin } from '../api'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

// 登录处理
const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await adminLogin(form.value.username, form.value.password)
    sessionStorage.setItem('admin', JSON.stringify(res.data))
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #201a3b, #6c5ce7);
}

.login-box {
  width: 380px;
  background: #fff;
  border-radius: 20px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-title {
  text-align: center;
  margin-bottom: 28px;
}

.login-title span {
  font-size: 30px;
  font-weight: 900;
  letter-spacing: -1px;
}

.login-title i {
  color: #6c5ce7;
  font-style: normal;
}

.login-title p {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}
</style>
