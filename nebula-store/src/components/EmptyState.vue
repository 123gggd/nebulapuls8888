<template>
  <el-empty :description="description" class="empty-state">
    <template #image>
      <slot name="image" />
    </template>
    <slot>
      <el-button v-if="actionText" type="primary" @click="handleAction">{{ actionText }}</el-button>
    </slot>
  </el-empty>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  description: string
  actionText?: string
  actionTo?: string
}

const props = defineProps<Props>()
const router = useRouter()

const handleAction = () => {
  if (props.actionTo) {
    router.push(props.actionTo)
  }
}
</script>

<style scoped lang="scss">
.empty-state {
  padding: 30px 0;
  :deep(.el-empty__image) {
    margin-bottom: 10px;
  }
}
</style>
