<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <div class="stat-card" :style="{ background: card.color }">
          <div class="label">{{ card.label }}</div>
          <div class="num">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <div class="table-card">
          <h3 style="margin-bottom: 16px">业务数据分布</h3>
          <div ref="barRef" style="height: 320px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="table-card">
          <h3 style="margin-bottom: 16px">内容与风控概览</h3>
          <div ref="pieRef" style="height: 320px"></div>
        </div>
      </el-col>
    </el-row>

    <div class="table-card" style="margin-top: 16px">
      <h3 style="margin-bottom: 16px">核心指标明细</h3>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="累计注册用户">{{ data.totalUsers }}</el-descriptions-item>
        <el-descriptions-item label="匹配成功数">{{ data.matchCount }}</el-descriptions-item>
        <el-descriptions-item label="聊天会话数">{{ data.sessionCount }}</el-descriptions-item>
        <el-descriptions-item label="消息总数">{{ data.messageCount }}</el-descriptions-item>
        <el-descriptions-item label="动态数">{{ data.postCount }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ data.likeCount }}</el-descriptions-item>
        <el-descriptions-item label="评论数">{{ data.commentCount }}</el-descriptions-item>
        <el-descriptions-item label="跑腿总量">{{ data.errandTotal }}</el-descriptions-item>
        <el-descriptions-item label="跑腿完成量">{{ data.errandFinished }}</el-descriptions-item>
        <el-descriptions-item label="二手商品数">{{ data.secondCount }}</el-descriptions-item>
        <el-descriptions-item label="商城订单量">{{ data.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="商城成交额">¥ {{ data.gmv }}</el-descriptions-item>
        <el-descriptions-item label="举报量">{{ data.reportCount }}</el-descriptions-item>
        <el-descriptions-item label="违规量">{{ data.violationCount }}</el-descriptions-item>
        <el-descriptions-item label="封禁量">{{ data.banCount }}</el-descriptions-item>
        <el-descriptions-item label="退款量">{{ data.refundCount }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statOverview } from '../api'

const data = ref({})
const barRef = ref(null)
const pieRef = ref(null)

const cards = computed(() => [
  { label: '累计注册用户', value: data.value.totalUsers || 0, color: 'linear-gradient(135deg,#6c5ce7,#a29bfe)' },
  { label: '匹配成功数', value: data.value.matchCount || 0, color: 'linear-gradient(135deg,#ff557c,#ff9a9e)' },
  { label: '商城成交额', value: '¥' + (data.value.gmv || 0), color: 'linear-gradient(135deg,#00b894,#55efc4)' },
  { label: '举报待处理', value: data.value.reportCount || 0, color: 'linear-gradient(135deg,#fdcb6e,#ffeaa7)' }
])

// 加载数据并渲染图表
const load = async () => {
  const res = await statOverview()
  data.value = res.data
  await nextTick()
  renderBar()
  renderPie()
}

const renderBar = () => {
  const chart = echarts.init(barRef.value)
  chart.setOption({
    tooltip: {},
    xAxis: {
      type: 'category',
      data: ['动态', '匹配', '会话', '跑腿', '二手', '订单']
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [
        data.value.postCount, data.value.matchCount, data.value.sessionCount,
        data.value.errandTotal, data.value.secondCount, data.value.orderCount
      ],
      itemStyle: { color: '#6c5ce7', borderRadius: [6, 6, 0, 0] }
    }]
  })
}

const renderPie = () => {
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      data: [
        { value: data.value.likeCount, name: '点赞' },
        { value: data.value.commentCount, name: '评论' },
        { value: data.value.reportCount, name: '举报' },
        { value: data.value.violationCount, name: '违规' },
        { value: data.value.banCount, name: '封禁' }
      ],
      color: ['#6c5ce7', '#00b894', '#fdcb6e', '#ff557c', '#636e72']
    }]
  })
}

onMounted(load)
</script>
