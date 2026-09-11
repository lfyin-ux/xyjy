<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="商品名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="openDialog()">新增商品</el-button>
          <el-button @click="openCategory">分类管理</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="封面" width="90">
          <template #default="{ row }">
            <el-image v-if="row.cover" :src="fileUrl(row.cover)" fit="cover" style="width: 60px; height: 45px" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" width="90" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
              @click="setStatus(row.id, row.status === 1 ? 0 : 1)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total"
          :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPage" />
      </div>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑商品' : '新增商品'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="多个规格用逗号分隔" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload :action="uploadUrl" :data="{ bizDir: 'mall' }" :show-file-list="false"
            :on-success="onCoverSuccess" accept="image/*">
            <el-image v-if="form.cover" :src="fileUrl(form.cover)" fit="cover" style="width: 90px; height: 90px" />
            <el-button v-else>上传封面</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input v-model="form.detail" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialog" title="分类管理" width="440px">
      <div style="margin-bottom: 12px">
        <el-input v-model="newCategory" placeholder="新分类名称" style="width: 70%">
          <template #append>
            <el-button @click="addCategory">添加</el-button>
          </template>
        </el-input>
      </div>
      <el-table :data="categories" size="small" border>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="delCategory(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  goodsList, goodsSave, goodsSetStatus, goodsDelete,
  mallCategories, categorySave, categoryDelete, uploadUrl
} from '../api'
import { fileUrl } from '../utils/file'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '' })
const dialog = ref(false)
const form = ref({})
const categories = ref([])
const categoryDialog = ref(false)
const newCategory = ref('')

const load = async () => {
  loading.value = true
  try {
    const res = await goodsList(query.value)
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

const loadCategories = async () => {
  const res = await mallCategories()
  categories.value = res.data
}

const openDialog = (row) => {
  form.value = row ? { ...row } : { price: 0, stock: 0, status: 1 }
  dialog.value = true
}

const onCoverSuccess = (res) => {
  form.value.cover = res.data.url
  ElMessage.success('封面上传成功')
}

const doSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写商品名称')
    return
  }
  await goodsSave(form.value)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

const setStatus = async (id, status) => {
  await goodsSetStatus(id, status)
  ElMessage.success('操作成功')
  load()
}

const doDelete = (id) => {
  ElMessageBox.confirm('确定删除该商品吗？', '提示').then(async () => {
    await goodsDelete(id)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

const openCategory = () => {
  categoryDialog.value = true
}

const addCategory = async () => {
  if (!newCategory.value) return
  await categorySave({ name: newCategory.value })
  newCategory.value = ''
  ElMessage.success('已添加')
  loadCategories()
}

const delCategory = (id) => {
  ElMessageBox.confirm('确定删除该分类吗？', '提示').then(async () => {
    await categoryDelete(id)
    ElMessage.success('已删除')
    loadCategories()
  }).catch(() => {})
}

onMounted(() => {
  load()
  loadCategories()
})
</script>
