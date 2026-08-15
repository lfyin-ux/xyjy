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
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ row.user?.nickname }}</template>
        </el-table-column>
        <el-table-column label="学校" width="160">
          <template #default="{ row }">{{ row.auth.schoolName }}</template>
        </el-table-column>
        <el-table-column label="院系" width="120">
          <template #default="{ row }">{{ row.auth.college }}</template>
        </el-table-column>
        <el-table-column label="学号" width="120">
          <template #default="{ row }">{{ row.auth.studentNo }}</template>
        </el-table-column>
        <el-table-column label="材料类型" width="120">
          <template #default="{ row }">{{ row.auth.docType }}</template>
        </el-table-column>
        <el-table-column label="证明材料">
          <template #default="{ row }">
            <el-image v-for="(img, i) in splitImgs(row.auth.docImgs)" :key="i" :src="img" fit="cover"
              style="width: 60px; height: 40px; margin-right: 6px" :preview-src-list="splitImgs(row.auth.docImgs)" />
          </template>
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
import { schoolAuditList, schoolPass, schoolReject } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: 1 })

const statusText = (s) => (s === 2 ? '已通过' : s === 3 ? '已驳回' : '审核中')
const statusType = (s) => (s === 2 ? 'success' : s === 3 ? 'danger' : 'warning')
const splitImgs = (s) => (s ? s.split(',').filter(Boolean) : [])

const load = async () => {
  loading.value = true
  try {
    const res = await schoolAuditList(query.value)
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
  await schoolPass(id)
  ElMessage.success('审核通过，已绑定学校')
  load()
}

const doReject = (id) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回认证').then(async ({ value }) => {
    await schoolReject(id, value)
    ElMessage.success('已驳回')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
