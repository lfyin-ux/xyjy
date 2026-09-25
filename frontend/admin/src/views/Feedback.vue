<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="onFilter">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待处理</el-radio-button>
        <el-radio-button :value="1">已处理</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="content" label="反馈内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="140">
          <template #default="{ row }">{{ row.contact || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="success" @click="doHandle(row.id)">标记已处理</el-button>
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
import { ElMessage } from 'element-plus'
import { feedbackList, handleFeedback } from '../api'
import { formatTime } from '../utils/time'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: null })

const load = async () => {
  loading.value = true
  try {
    const res = await feedbackList(query.value)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const onFilter = () => {
  query.value.pageNum = 1
  load()
}

const onPage = (p) => {
  query.value.pageNum = p
  load()
}

const doHandle = async (id) => {
  await handleFeedback(id)
  ElMessage.success('已标记为已处理')
  load()
}

onMounted(load)
</script>
