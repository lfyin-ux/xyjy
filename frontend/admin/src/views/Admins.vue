<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="openDialog()">新增管理员</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="nickname" label="姓名" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)"
              v-if="row.id !== 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="table-card" style="margin-top: 16px">
      <h3 style="margin-bottom: 12px">角色与权限</h3>
      <el-table :data="roles" size="small" border>
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="remark" label="说明" />
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑管理员' : '新增管理员'" width="440px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminList, adminSave, adminDelete, roleList } from '../api'

const list = ref([])
const roles = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({})

const load = async () => {
  loading.value = true
  try {
    const res = await adminList()
    list.value = res.data
    const r = await roleList()
    roles.value = r.data
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  form.value = row ? { ...row } : { status: 1 }
  dialog.value = true
}

const doSave = async () => {
  if (!form.value.username) {
    ElMessage.warning('请填写账号')
    return
  }
  await adminSave(form.value)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

const doDelete = (id) => {
  ElMessageBox.confirm('确定删除该管理员吗？', '提示').then(async () => {
    await adminDelete(id)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
