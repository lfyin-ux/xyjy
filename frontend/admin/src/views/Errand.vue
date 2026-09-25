<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="load">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">待接单</el-radio-button>
        <el-radio-button :value="2">进行中</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
        <el-radio-button :value="4">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="publisherName" label="发单人" width="110" />
        <el-table-column prop="takerName" label="接单人" width="110" />
        <el-table-column label="路线" width="200">
          <template #default="{ row }">{{ row.fromPlace }} → {{ row.toPlace }}</template>
        </el-table-column>
        <el-table-column prop="fee" label="赏金" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="setStatus(row.id, 4)"
              v-if="row.status !== 4 && row.status !== 3">取消订单</el-button>
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
import { errandList, errandSetStatus } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: null })

const statusText = (s) => ({ 1: '待接单', 2: '进行中', 3: '已完成', 4: '已取消' }[s] || s)
const statusType = (s) => ({ 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await errandList(query.value)
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

const setStatus = async (id, status) => {
  await errandSetStatus(id, status)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
