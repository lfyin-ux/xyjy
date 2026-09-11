<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="昵称/手机号/ID" clearable />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="query.school" placeholder="学校" clearable />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="限制发言" :value="2" />
            <el-option label="封禁" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="fileUrl(row.avatar)" shape="square">{{ row.nickname?.charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="110" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="school" label="学校" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="认证" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.identityVerified" size="small" type="success">实名</el-tag>
            <el-tag v-else size="small" type="info">未实名</el-tag>
            <el-tag v-if="row.schoolVerified" size="small" type="success" style="margin-left:4px">学校</el-tag>
            <el-tag v-else size="small" type="info" style="margin-left:4px">未认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row.id)">详情</el-button>
            <el-button size="small" type="warning" @click="doWarn(row)">警告</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="doBan(row)">封禁</el-button>
            <el-button v-else size="small" type="success" @click="doUnban(row)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total"
          :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPage" />
      </div>
    </div>

    <el-drawer v-model="drawer" title="用户详情" size="520px">
      <div v-if="detail.user">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="昵称">{{ detail.user.nickname }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ detail.user.school }} {{ detail.user.college }}</el-descriptions-item>
          <el-descriptions-item label="简介">
            {{ detail.user.intro }}
            <el-button v-if="detail.user.introAuditStatus === 0" size="small" type="primary"
              @click="doAuditProfile(detail.user.id, 'intro', true)">通过简介</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="标签">{{ detail.user.tags }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">相册照片</h4>
        <div class="photo-list">
          <div v-for="p in detail.photos" :key="p.id" class="photo-item">
            <el-image :src="fileUrl(p.imgUrl)" fit="cover" style="width: 90px; height: 90px; border-radius: 8px" />
            <div>
              <el-tag size="small" :type="p.auditStatus === 1 ? 'success' : 'info'">
                {{ p.auditStatus === 1 ? '已通过' : p.auditStatus === 2 ? '已驳回' : '待审核' }}
              </el-tag>
              <el-button v-if="p.auditStatus === 0" size="small" @click="doAuditPhoto(p.id, true)">通过</el-button>
            </div>
          </div>
          <el-empty v-if="!detail.photos || !detail.photos.length" description="暂无相册" :image-size="60" />
        </div>

        <h4 style="margin: 16px 0 8px">违规记录</h4>
        <el-table :data="detail.violations" size="small" border>
          <el-table-column prop="type" label="类型" />
          <el-table-column prop="reason" label="原因" />
          <el-table-column prop="createTime" label="时间" />
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userList, userDetail, warnUser, banUser, unbanUser, auditProfile, auditPhoto } from '../api'
import { fileUrl } from '../utils/file'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', school: '', status: null })
const drawer = ref(false)
const detail = ref({})

const genderText = (g) => (g === 1 ? '男' : g === 2 ? '女' : '未知')
const statusText = (s) => (s === 1 ? '正常' : s === 2 ? '限制发言' : '封禁')
const statusType = (s) => (s === 1 ? 'success' : s === 2 ? 'warning' : 'danger')

const load = async () => {
  loading.value = true
  try {
    const res = await userList(query.value)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.value = { pageNum: 1, pageSize: 10, keyword: '', school: '', status: null }
  load()
}

const onPage = (p) => {
  query.value.pageNum = p
  load()
}

const showDetail = async (id) => {
  const res = await userDetail(id)
  detail.value = res.data
  drawer.value = true
}

const doWarn = (row) => {
  ElMessageBox.prompt('请输入警告原因', '警告用户').then(async ({ value }) => {
    await warnUser(row.id, value)
    ElMessage.success('已警告')
  }).catch(() => {})
}

const doBan = (row) => {
  ElMessageBox.prompt('请输入封禁原因', '封禁用户').then(async ({ value }) => {
    await banUser(row.id, value)
    ElMessage.success('已封禁')
    load()
  }).catch(() => {})
}

const doUnban = async (row) => {
  await unbanUser(row.id)
  ElMessage.success('已解封')
  load()
}

const doAuditProfile = async (userId, type, pass) => {
  await auditProfile(userId, type, pass)
  ElMessage.success('审核完成')
  showDetail(userId)
}

const doAuditPhoto = async (photoId, pass) => {
  await auditPhoto(photoId, pass)
  ElMessage.success('审核完成')
  showDetail(detail.value.user.id)
}

onMounted(load)
</script>

<style scoped>
.photo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.photo-item {
  text-align: center;
}
</style>
