<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="query.status" @change="load">
        <el-radio-button :value="1">审核中</el-radio-button>
        <el-radio-button :value="2">已通过</el-radio-button>
        <el-radio-button :value="3">已驳回</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="名称" width="120">
          <template #default="{ row }">{{ row.user?.nickname }}</template>
        </el-table-column>
        <el-table-column label="学校" width="160">
          <template #default="{ row }">
            {{ row.auth.schoolName }}
            <el-tag v-if="row.auth.schoolId" size="small" type="info" style="margin-left:4px">已选ID</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="院系" width="120">
          <template #default="{ row }">{{ row.auth.college }}</template>
        </el-table-column>
        <el-table-column label="学号" width="120">
          <template #default="{ row }">{{ row.auth.studentNo }}</template>
        </el-table-column>
        <el-table-column label="材料类型" width="120">
          <template #default="{ row }">{{ row.auth.docType }}</template>
        </el-table-column>
        <el-table-column label="证明材料">
          <template #default="{ row }">
            <el-image v-for="(img, i) in splitImgs(row.auth.docImgs)" :key="i" :src="img" fit="cover"
              style="width: 60px; height: 40px; margin-right: 6px" :preview-src-list="splitImgs(row.auth.docImgs)" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.auth.status)">{{ statusText(row.auth.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.auth.status === 1">
              <el-button size="small" type="success" @click="openPass(row)">通过</el-button>
              <el-button size="small" type="danger" @click="doReject(row.auth.id)">驳回</el-button>
            </template>
            <span v-else>{{ row.auth.rejectReason || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total"
          :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPage" />
      </div>
    </div>

    <el-dialog v-model="passVisible" title="确认所属学校" width="480px">
      <div style="margin-bottom:12px;color:#666;font-size:13px">
        用户填写：{{ passRow?.auth?.schoolName || '-' }}
      </div>
      <el-form label-width="100px">
        <el-form-item label="标准学校">
          <el-select
            v-model="passForm.schoolId"
            filterable
            remote
            clearable
            placeholder="搜索已有学校"
            :remote-method="searchSchools"
            :loading="schoolLoading"
            style="width:100%"
            @change="onSchoolPick"
          >
            <el-option v-for="s in schoolOptions" :key="s.id" :label="s.schoolName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="或新建学校">
          <el-input v-model="passForm.schoolName" placeholder="输入标准校名后通过将自动创建" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPass">确认通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { schoolAuditList, schoolPass, schoolReject, schoolDictList } from '../api'
import { splitFileUrls } from '../utils/file'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: 1 })

const passVisible = ref(false)
const passRow = ref(null)
const passForm = ref({ schoolId: null, schoolName: '' })
const schoolOptions = ref([])
const schoolLoading = ref(false)

const statusText = (s) => (s === 2 ? '已通过' : s === 3 ? '已驳回' : '审核中')
const statusType = (s) => (s === 2 ? 'success' : s === 3 ? 'danger' : 'warning')
const splitImgs = splitFileUrls

const load = async () => {
  loading.value = true
  try {
    const res = await schoolAuditList(query.value)
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

const searchSchools = async (keyword) => {
  schoolLoading.value = true
  try {
    const res = await schoolDictList(keyword)
    schoolOptions.value = res.data || []
  } finally {
    schoolLoading.value = false
  }
}

const onSchoolPick = (id) => {
  if (!id) return
  const hit = schoolOptions.value.find((s) => s.id === id)
  if (hit) passForm.value.schoolName = ''
}

const openPass = (row) => {
  passRow.value = row
  passForm.value = {
    schoolId: row.auth.schoolId || null,
    schoolName: row.auth.schoolId ? '' : (row.auth.schoolName || '')
  }
  passVisible.value = true
  searchSchools(row.auth.schoolName || '')
}

const confirmPass = async () => {
  const { schoolId, schoolName } = passForm.value
  if (!schoolId && !schoolName?.trim()) {
    ElMessage.warning('请选择已有学校或填写标准校名')
    return
  }
  await schoolPass(passRow.value.auth.id, schoolId, schoolName?.trim() || '')
  ElMessage.success('审核通过，已绑定学校')
  passVisible.value = false
  load()
}

const doReject = (id) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回认证').then(async ({ value }) => {
    await schoolReject(id, value)
    ElMessage.success('已驳回')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
