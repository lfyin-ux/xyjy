<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="load">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">申请中</el-radio-button>
        <el-radio-button :value="1">已同意</el-radio-button>
        <el-radio-button :value="2">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="goodsName" label="商品" />
        <el-table-column prop="spec" label="规格" width="80" />
        <el-table-column prop="quantity" label="退货数量" width="90" />
        <el-table-column label="退款金额" width="100">
          <template #default="{ row }">¥{{ row.refundAmount }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="doHandle(row.id, true)">同意</el-button>
              <el-button size="small" type="danger" @click="doHandle(row.id, false)">拒绝</el-button>
            </template>
            <span v-else>{{ row.rejectReason || '-' }}</span>
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
import request from '../api/request'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: null })

const statusText = (s) => ({ 0: '申请中', 1: '已同意', 2: '已拒绝' }[s] || '')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/refund/admin/list', { params: query.value })
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

const doHandle = (id, pass) => {
  if (pass) {
    ElMessageBox.confirm('确定同意该退款申请吗？', '同意退款').then(async () => {
      await request.post(`/refund/admin/handle/${id}?pass=true`)
      ElMessage.success('已同意退款')
      load()
    }).catch(() => {})
  } else {
    ElMessageBox.prompt('请输入拒绝原因', '拒绝退款').then(async ({ value }) => {
      await request.post(`/refund/admin/handle/${id}?pass=false&rejectReason=${encodeURIComponent(value || '')}`)
      ElMessage.success('已拒绝')
      load()
    }).catch(() => {})
  }
}

onMounted(load)
</script>
