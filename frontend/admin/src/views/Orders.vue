<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" placeholder="订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="待付款" :value="1" />
            <el-option label="待备货" :value="2" />
            <el-option label="配送中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="退款售后" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <el-table :data="row.items" size="small" border style="margin: 10px 40px">
              <el-table-column label="商品" prop="goodsName" />
              <el-table-column label="规格" prop="spec" width="120" />
              <el-table-column label="单价" prop="price" width="100" />
              <el-table-column label="数量" prop="quantity" width="80" />
            </el-table>
          </template>
        </el-table-column>
        <el-table-column label="订单号" width="180">
          <template #default="{ row }">{{ row.order.orderNo }}</template>
        </el-table-column>
        <el-table-column label="用户ID" width="90">
          <template #default="{ row }">{{ row.order.userId }}</template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥ {{ row.order.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="收货人" width="120">
          <template #default="{ row }">{{ row.order.receiver }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.order.status)">{{ statusText(row.order.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退款" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.order.refundStatus > 0" :type="refundType(row.order.refundStatus)">
              {{ refundText(row.order.refundStatus) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="doShip(row.order.id)"
              v-if="row.order.status === 2">发货</el-button>
            <template v-if="row.order.refundStatus === 1">
              <el-button size="small" type="success" @click="doRefund(row.order.id, true)">同意退款</el-button>
              <el-button size="small" @click="doRefund(row.order.id, false)">拒绝</el-button>
            </template>
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
import { orderList, orderShip, orderRefund } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, orderNo: '', status: null })

const statusText = (s) => ({ 1: '待付款', 2: '待备货', 3: '配送中', 4: '已完成', 5: '退款售后' }[s] || s)
const statusType = (s) => ({ 1: 'warning', 2: 'primary', 3: 'primary', 4: 'success', 5: 'danger' }[s] || 'info')
const refundText = (s) => ({ 1: '申请中', 2: '已退款', 3: '已拒绝' }[s] || '')
const refundType = (s) => ({ 1: 'warning', 2: 'success', 3: 'info' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await orderList(query.value)
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

const doShip = async (id) => {
  await orderShip(id)
  ElMessage.success('已发货')
  load()
}

const doRefund = async (id, pass) => {
  await orderRefund(id, pass)
  ElMessage.success(pass ? '已同意退款' : '已拒绝退款')
  load()
}

onMounted(load)
</script>
