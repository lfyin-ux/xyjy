<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="业务类型">
          <el-select v-model="query.bizType" placeholder="全部" clearable style="width: 130px" @change="load">
            <el-option label="动态" value="动态" />
            <el-option label="评论" value="评论" />
            <el-option label="聊天" value="聊天" />
            <el-option label="简介" value="简介" />
            <el-option label="二手商品" value="二手商品" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="word" label="命中词" width="120" />
        <el-table-column prop="content" label="命中内容" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="bizType" label="业务类型" width="100" />
        <el-table-column prop="handleResult" label="处理结果" width="120" />
        <el-table-column prop="createTime" label="时间" width="180" />
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
import { hitLogList } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, bizType: null })

const load = async () => {
  loading.value = true
  try {
    const res = await hitLogList(query.value)
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

onMounted(load)
</script>
