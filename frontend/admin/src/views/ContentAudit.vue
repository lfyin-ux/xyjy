<template>
  <div class="page-container">
    <el-tabs v-model="tab">
      <el-tab-pane label="动态审核" name="post">
        <div class="table-card">
          <el-table :data="postList" v-loading="loading" stripe>
            <el-table-column label="发布者" width="120">
              <template #default="{ row }">{{ row.user?.nickname }}</template>
            </el-table-column>
            <el-table-column label="内容">
              <template #default="{ row }">{{ row.post.content }}</template>
            </el-table-column>
            <el-table-column label="图片" width="120">
              <template #default="{ row }">
                <el-image v-if="row.post.images" :src="firstImg(row.post.images)" fit="cover"
                  style="width: 60px; height: 45px" :preview-src-list="splitImgs(row.post.images)" />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="话题" width="90">
              <template #default="{ row }">{{ row.post.topic }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="doPostPass(row.post.id)">通过</el-button>
                <el-button size="small" type="danger" @click="doPostReject(row.post.id)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!postList.length" description="暂无待审核动态" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="评论审核" name="comment">
        <div class="table-card">
          <el-table :data="commentList" v-loading="loading" stripe>
            <el-table-column prop="postId" label="动态ID" width="90" />
            <el-table-column prop="content" label="评论内容" />
            <el-table-column prop="createTime" label="时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="doCommentAudit(row.id, true)">通过</el-button>
                <el-button size="small" type="danger" @click="doCommentAudit(row.id, false)">拦截</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!commentList.length" description="暂无待审核评论" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { postAuditList, postPass, postReject, commentAuditList, commentAudit } from '../api'

const tab = ref('post')
const postList = ref([])
const commentList = ref([])
const loading = ref(false)

const firstImg = (s) => (s ? s.split(',')[0] : '')
const splitImgs = (s) => (s ? s.split(',').filter(Boolean) : [])

const load = async () => {
  loading.value = true
  try {
    const res1 = await postAuditList({ pageNum: 1, pageSize: 50 })
    postList.value = res1.data.records
    const res2 = await commentAuditList()
    commentList.value = res2.data
  } finally {
    loading.value = false
  }
}

const doPostPass = async (id) => {
  await postPass(id)
  ElMessage.success('已通过')
  load()
}

const doPostReject = (id) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回动态').then(async ({ value }) => {
    await postReject(id, value)
    ElMessage.success('已驳回')
    load()
  }).catch(() => {})
}

const doCommentAudit = async (id, pass) => {
  await commentAudit(id, pass)
  ElMessage.success(pass ? '已通过' : '已拦截')
  load()
}

onMounted(load)
</script>
