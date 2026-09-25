<template>
  <div class="page-container">
    <el-tabs v-model="tab">
      <el-tab-pane label="动态管理" name="post">
        <div class="search-bar">
          <el-form :inline="true">
            <el-form-item label="关键词">
              <el-input v-model="query.keyword" placeholder="动态内容" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
                <el-option label="已发布" :value="3" />
                <el-option label="待审核" :value="1" />
                <el-option label="审核驳回" :value="4" />
                <el-option label="违规拦截" :value="5" />
                <el-option label="已删除" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadPosts">查询</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="table-card">
          <el-table :data="postList" v-loading="loading" stripe>
            <el-table-column prop="userName" label="名称" width="110" />
            <el-table-column prop="content" label="内容" />
            <el-table-column prop="topic" label="话题" width="90" />
            <el-table-column prop="likeCount" label="点赞" width="70" />
            <el-table-column prop="commentCount" label="评论" width="70" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="postStatusType(row.status)">{{ postStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="setStatus(row.id, 6)" v-if="row.status !== 6">删除</el-button>
                <el-button size="small" type="success" @click="setStatus(row.id, 3)"
                  v-if="row.status !== 3">发布</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next" :total="postTotal"
              :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPostPage" />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="话题标签" name="topic">
        <div class="search-bar">
          <el-button type="success" @click="openTopic()">新增标签</el-button>
        </div>
        <div class="table-card">
          <el-table :data="topics" stripe>
            <el-table-column prop="name" label="标签名称" />
            <el-table-column prop="postCount" label="关联动态数" width="120" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openTopic(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delTopic(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="topicDialog" :title="topicForm.id ? '编辑标签' : '新增标签'" width="400px">
      <el-form :model="topicForm" label-width="90px">
        <el-form-item label="标签名称">
          <el-input v-model="topicForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTopic">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { squareList, squareSetStatus, topicList, topicSave, topicDelete } from '../api'

const tab = ref('post')
const postList = ref([])
const postTotal = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', status: null })
const topics = ref([])
const topicDialog = ref(false)
const topicForm = ref({})

const postStatusText = (s) => ({ 0: '草稿', 1: '待审核', 3: '已发布', 4: '审核驳回', 5: '违规拦截', 6: '已删除' }[s] || s)
const postStatusType = (s) => ({ 3: 'success', 1: 'warning', 4: 'danger', 5: 'danger', 6: 'info' }[s] || 'info')

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await squareList(query.value)
    postList.value = res.data.records
    postTotal.value = res.data.total
  } finally {
    loading.value = false
  }
}

const onPostPage = (p) => {
  query.value.pageNum = p
  loadPosts()
}

const setStatus = async (id, status) => {
  await squareSetStatus(id, status)
  ElMessage.success('操作成功')
  loadPosts()
}

const loadTopics = async () => {
  const res = await topicList()
  topics.value = res.data
}

const openTopic = (row) => {
  topicForm.value = row ? { ...row } : {}
  topicDialog.value = true
}

const saveTopic = async () => {
  if (!topicForm.value.name) {
    ElMessage.warning('请填写标签名称')
    return
  }
  await topicSave(topicForm.value)
  ElMessage.success('保存成功')
  topicDialog.value = false
  loadTopics()
}

const delTopic = (id) => {
  ElMessageBox.confirm('确定删除该标签吗？', '提示').then(async () => {
    await topicDelete(id)
    ElMessage.success('已删除')
    loadTopics()
  }).catch(() => {})
}

onMounted(() => {
  loadPosts()
  loadTopics()
})
</script>
