<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="load">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">在售</el-radio-button>
        <el-radio-button :value="2">已下架</el-radio-button>
        <el-radio-button :value="5">违规</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <el-image v-if="row.images" :src="firstImg(row.images)" fit="cover"
              style="width: 60px; height: 45px" :preview-src-list="splitImgs(row.images)" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="conditionDesc" label="成色" width="90" />
        <el-table-column prop="price" label="价格" width="90" />
        <el-table-column prop="sellerId" label="卖家ID" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="doPass(row.id)" v-if="row.status === 0">审核通过</el-button>
            <el-button size="small" @click="setStatus(row.id, 2)" v-if="row.status === 1">下架</el-button>
            <el-button size="small" type="danger" @click="setStatus(row.id, 5)"
              v-if="row.status !== 5">违规</el-button>
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
import { secondList, secondPass, secondSetStatus } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: null })

const firstImg = (s) => (s ? s.split(',')[0] : '')
const splitImgs = (s) => (s ? s.split(',').filter(Boolean) : [])
const statusText = (s) => ({ 0: '待审核', 1: '在售', 2: '已下架', 5: '违规' }[s] || s)
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 5: 'danger' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await secondList(query.value)
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
  await secondPass(id)
  ElMessage.success('审核通过')
  load()
}

const setStatus = async (id, status) => {
  await secondSetStatus(id, status)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
