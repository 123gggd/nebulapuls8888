<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <div class="desc">抱歉，您访问的页面迷失在太空中了</div>
      <el-button type="primary" size="large" @click="$router.push('/')">返回地球</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  text-align: center;
}
.code {
  font-size: 120px;
  font-weight: 900;
  color: #409EFF;
  line-height: 1.2;
  text-shadow: 4px 4px 0px rgba(64,158,255,0.2);
}
.desc {
  font-size: 24px;
  color: #606266;
  margin-bottom: 40px;
}
</style>