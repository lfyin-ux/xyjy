<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="load">
        <el-radio-button :value="1">审核中</el-radio-button>
        <el-radio-button :value="2">已通过</el-radio-button>
        <el-radio-button :value="3">已驳回</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="昵称" width="120">
          <template #default="{ row }">{{ row.user?.nickname || '-' }}</template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">{{ row.auth?.phone || '-' }}</template>
        </el-table-column>
        <el-table-column label="真实姓名" width="100">
          <template #default="{ row }">{{ row.auth?.realName || '-' }}</template>
        </el-table-column>
        <el-table-column label="身份证号" min-width="180">
          <template #default="{ row }">{{ row.auth?.idCard || '-' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ row.auth?.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.auth.status)">{{ statusText(row.auth.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.auth.status === 1">
              <el-button size="small" type="success" @click="doPass(row.auth.id)">通过</el-button>
              <el-button size="small" type="danger" @click="doReject(row.auth.id)">驳回</el-button>
            </template>
            <span v-else>{{ row.auth.rejectReason || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total"
          :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPage" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { personalAuditList, personalPass, personalReject } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: 1 })

const statusText = (s) => (s === 2 ? '已通过' : s === 3 ? '已驳回' : '审核中')
const statusType = (s) => (s === 2 ? 'success' : s === 3 ? 'danger' : 'warning')

const load = async () => {
  loading.value = true
  try {
    const res = await personalAuditList(query.value)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const onPage = (p) => {
  query.value.pageNum = p
  load()
}

const doPass = async (id) => {
  await ElMessageBox.confirm('通过后用户可获得 2 小时内发布 1 条动态的权限，是否确认？', '通过个人认证')
  await personalPass(id)
  ElMessage.success('已通过')
  load()
}

const doReject = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回个人认证', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请填写驳回原因'
  })
  await personalReject(id, value)
  ElMessage.success('已驳回')
  load()
}

onMounted(load)
</script>
