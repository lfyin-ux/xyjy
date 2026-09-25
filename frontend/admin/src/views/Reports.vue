<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="load">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待处理</el-radio-button>
        <el-radio-button :value="1">已处理</el-radio-button>
        <el-radio-button :value="2">已驳回</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="reporterName" label="举报人" width="110" />
        <el-table-column label="举报类型" width="110">
          <template #default="{ row }">{{ typeText(row.targetType) }}</template>
        </el-table-column>
        <el-table-column prop="targetName" label="举报对象" width="120" />
        <el-table-column prop="reason" label="举报原因" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'">
              {{ row.status === 1 ? '已处理' : row.status === 2 ? '已驳回' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleResult" label="处理结果" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="doHandle(row.id, 1)">处理</el-button>
              <el-button size="small" @click="doHandle(row.id, 2)">驳回</el-button>
            </template>
            <span v-else>-</span>
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
import { reportList, handleReport } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: null })

const typeText = (t) => ({ post: '动态', comment: '评论', user: '用户', order: '订单' }[t] || t)

const load = async () => {
  loading.value = true
  try {
    const res = await reportList(query.value)
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

const doHandle = (id, status) => {
  ElMessageBox.prompt('请输入处理说明', status === 1 ? '处理举报' : '驳回举报').then(async ({ value }) => {
    await handleReport(id, status, value)
    ElMessage.success('操作完成')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
