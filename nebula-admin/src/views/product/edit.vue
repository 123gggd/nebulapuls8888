<template>
  <div class="page-container" style="padding: 20px;">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑商品' : '发布新商品' }}</span>
          <el-button style="float: right; padding: 3px 0" text @click="goBack">返回列表</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" label-width="100px" ref="formRef">

        <el-divider content-position="left">基础信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副标题" prop="subtitle">
              <el-input v-model="form.subtitle" placeholder="请输入副标题" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-tree-select
                  v-model="form.categoryId"
                  :data="categoryOptions"
                  :props="{ label: 'name', value: 'id', children: 'children' }"
                  check-strictly
                  placeholder="请选择分类"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示价格" prop="price">
              <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">图片信息</el-divider>

        <el-form-item label="商品主图" prop="mainImage">
          <el-upload
              class="avatar-uploader"
              action="#"
              :http-request="handleUploadMain"
              :show-file-list="false"
              :before-upload="beforeUpload"
          >
            <img v-if="form.mainImage" :src="form.mainImage" class="avatar hover-zoom" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="tips">建议尺寸 800x800，大小不超过 2MB</div>
        </el-form-item>

        <el-form-item label="轮播图">
          <!-- 暂未实现多图上传，可扩展 -->
          <div class="tips">暂支持单主图模式</div>
        </el-form-item>

        <el-divider content-position="left">详细描述</el-divider>

        <el-form-item label="商品详情">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="请输入商品详细描述..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存商品</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>

      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryTree, uploadFile, saveProduct, getProductDetail } from '@/api/product'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const categoryOptions = ref([])

// [修复] 只有当 params.id 存在且不为空字符串时，才视为 ID
const productId = route.params.id as string
const isEdit = computed(() => !!productId && productId !== '')

const form = reactive({
  id: null,
  name: '',
  subtitle: '',
  categoryId: null,
  price: 0,
  stock: 0,
  status: 1,
  mainImage: '',
  description: '',
  subImages: [] as string[]
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  mainImage: [{ required: true, message: '请上传主图', trigger: 'change' }]
}

// 加载初始数据
onMounted(async () => {
  // 1. 加载分类树
  const catRes: any = await getCategoryTree()
  categoryOptions.value = catRes.data

  // 2. 如果是编辑，加载详情
  // [修复] 增加对 isEdit 的判断，并且只有在 ID 有效时才调用接口
  if (isEdit.value) {
    const id = Number(productId)
    if (!isNaN(id)) {
      const prodRes: any = await getProductDetail(id)
      Object.assign(form, prodRes.data)
    }
  }
})

// 自定义上传逻辑
const handleUploadMain = async (options: any) => {
  try {
    const res: any = await uploadFile(options.file)
    // 后端返回的是完整URL，直接赋值
    form.mainImage = res.data
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) ElMessage.error('只能上传 JPG/PNG 格式!')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB!')
  return isJPG && isLt2M
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await saveProduct(form)
        ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
        goBack()
      } finally {
        submitting.value = false
      }
    }
  })
}

const goBack = () => router.push('/product/list')
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  text-align: center;
  line-height: 148px;
  border: 1px dashed #d9d9d9;
}
.tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
