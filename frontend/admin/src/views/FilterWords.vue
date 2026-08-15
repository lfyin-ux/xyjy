<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="词库类型">
          <el-select v-model="query.wordType" placeholder="全部" clearable style="width: 130px" @change="load">
            <el-option label="违规词" :value="1" />
            <el-option label="审核词" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="词语" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="openDialog()">新增词语</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="word" label="词语" width="140" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.wordType === 1 ? 'danger' : 'warning'">
              {{ row.wordType === 1 ? '违规词' : '审核词' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="匹配方式" width="100">
          <template #default="{ row }">{{ matchText(row.matchType) }}</template>
        </el-table-column>
        <el-table-column prop="scope" label="适用范围" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="(v) => doToggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total"
          :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPage" />
      </div>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑词语' : '新增词语'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="词语">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item label="词库类型">
          <el-radio-group v-model="form.wordType">
            <el-radio :value="1">违规词</el-radio>
            <el-radio :value="2">审核词</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如广告/色情/违法" />
        </el-form-item>
        <el-form-item label="匹配方式">
          <el-select v-model="form.matchType">
            <el-option label="精确匹配" :value="1" />
            <el-option label="模糊匹配" :value="2" />
            <el-option label="正则匹配" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用范围">
          <el-input v-model="form.scope" placeholder="动态,评论,聊天,简介" />
        </el-form-item>
        <el-form-item label="提示文案">
          <el-input v-model="form.tip" placeholder="命中后给用户的提示" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { filterList, filterSave, filterDelete, filterToggle } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, wordType: null, keyword: '' })
const dialog = ref(false)
const form = ref({})

const matchText = (m) => (m === 2 ? '模糊' : m === 3 ? '正则' : '精确')

const load = async () => {
  loading.value = true
  try {
    const res = await filterList(query.value)
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

const openDialog = (row) => {
  form.value = row ? { ...row } : { wordType: 1, matchType: 1, scope: '动态,评论,聊天,简介', enabled: 1 }
  dialog.value = true
}

const doSave = async () => {
  if (!form.value.word) {
    ElMessage.warning('请填写词语')
    return
  }
  await filterSave(form.value)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

const doDelete = (id) => {
  ElMessageBox.confirm('确定删除该词语吗？', '提示').then(async () => {
    await filterDelete(id)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

const doToggle = async (row, v) => {
  await filterToggle(row.id, v ? 1 : 0)
  row.enabled = v ? 1 : 0
  ElMessage.success('已更新')
}

onMounted(load)
</script>
