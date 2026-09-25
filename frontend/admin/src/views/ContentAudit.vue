<template>
  <div class="page-container">
    <el-tabs v-model="tab">
      <el-tab-pane label="动态审核" name="post">
        <div class="table-card">
          <el-table :data="postList" v-loading="loading" stripe>
            <el-table-column label="名称" width="120">
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
            <el-table-column label="所属动态" min-width="260">
              <template #default="{ row }">
                <div v-if="row.post" class="post-ref">
                  <div class="post-meta">
                    <span class="post-author">{{ row.postUser?.nickname || '未知用户' }}</span>
                    <span v-if="row.post.topic" class="post-topic">#{{ row.post.topic }}</span>
                    <span class="post-id">ID {{ row.post.id }}</span>
                  </div>
                  <div class="post-content">{{ row.post.content || '（无文字）' }}</div>
                  <el-image
                    v-if="row.post.images"
                    :src="firstImg(row.post.images)"
                    fit="cover"
                    class="post-thumb"
                    :preview-src-list="splitImgs(row.post.images)"
                  />
                </div>
                <span v-else class="muted">动态 #{{ row.comment.postId }}（已删除）</span>
              </template>
            </el-table-column>
            <el-table-column label="评论人" width="100">
              <template #default="{ row }">{{ row.commentUser?.nickname || '-' }}</template>
            </el-table-column>
            <el-table-column label="评论内容" min-width="160">
              <template #default="{ row }">{{ row.comment.content }}</template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ formatTime(row.comment.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="doCommentAudit(row.comment.id, true)">通过</el-button>
                <el-button size="small" type="danger" @click="doCommentAudit(row.comment.id, false)">拦截</el-button>
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
import { firstFileUrl, splitFileUrls } from '../utils/file'
import { formatTime } from '../utils/time'

const tab = ref('post')
const postList = ref([])
const commentList = ref([])
const loading = ref(false)

const firstImg = firstFileUrl
const splitImgs = splitFileUrls

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

<style scoped>
.post-ref {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #909399;
}

.post-author {
  color: #303133;
  font-weight: 600;
}

.post-topic {
  color: #409eff;
}

.post-content {
  color: #606266;
  line-height: 1.5;
  word-break: break-all;
}

.post-thumb {
  width: 56px;
  height: 56px;
  border-radius: 6px;
}

.muted {
  color: #c0c4cc;
}
</style>
