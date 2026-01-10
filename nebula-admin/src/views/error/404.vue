<template>
  <div class="lost-space">
    <div class="starfield"></div>
    <div class="nebula-glow"></div>
    <div class="lost-content">
      <div class="astronaut">
        <svg viewBox="0 0 300 300" role="img" aria-label="Lost astronaut">
          <defs>
            <linearGradient id="helmet" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#e0f2fe" />
              <stop offset="100%" stop-color="#94a3b8" />
            </linearGradient>
            <linearGradient id="suit" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#e2e8f0" />
              <stop offset="100%" stop-color="#cbd5f5" />
            </linearGradient>
          </defs>
          <circle cx="150" cy="150" r="88" fill="url(#helmet)" opacity="0.45" />
          <circle cx="150" cy="150" r="60" fill="#0f172a" opacity="0.6" />
          <path d="M110 170c18 16 62 16 80 0" stroke="#e2e8f0" stroke-width="10" fill="none" stroke-linecap="round" />
          <path d="M90 210c-6 26 14 50 40 50h40c26 0 46-24 40-50l-8-32h-104l-8 32z" fill="url(#suit)" />
          <circle cx="120" cy="210" r="10" fill="#60a5fa" />
          <circle cx="180" cy="210" r="10" fill="#f472b6" />
        </svg>
      </div>
      <div class="lost-text">
        <span class="signal">SIGNAL LOST</span>
        <h1>信号丢失</h1>
        <p>我们无法定位您请求的坐标，当前航线已偏离星云航道。</p>
        <el-button class="return-button" type="primary" size="large" @click="goHome">
          返回指挥舱
        </el-button>
      </div>
      <div class="lost-code">404</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.lost-space {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: radial-gradient(circle at top, rgba(15, 23, 42, 0.9), rgba(2, 6, 23, 0.98));
  color: #e2e8f0;
}

.starfield,
.nebula-glow {
  position: absolute;
  inset: 0;
}

.starfield {
  background-image:
    radial-gradient(2px 2px at 20% 30%, rgba(255, 255, 255, 0.6) 50%, transparent 52%),
    radial-gradient(1px 1px at 80% 20%, rgba(255, 255, 255, 0.5) 50%, transparent 52%),
    radial-gradient(1.5px 1.5px at 65% 70%, rgba(255, 255, 255, 0.4) 50%, transparent 52%),
    radial-gradient(2px 2px at 35% 80%, rgba(255, 255, 255, 0.35) 50%, transparent 52%),
    radial-gradient(1px 1px at 50% 50%, rgba(255, 255, 255, 0.5) 50%, transparent 52%);
  opacity: 0.6;
  animation: stars-float 18s linear infinite;
}

.nebula-glow {
  background: radial-gradient(circle at 20% 20%, rgba(56, 189, 248, 0.25), transparent 45%),
    radial-gradient(circle at 80% 30%, rgba(192, 132, 252, 0.3), transparent 40%),
    radial-gradient(circle at 50% 80%, rgba(99, 102, 241, 0.2), transparent 45%);
  filter: blur(10px);
  opacity: 0.7;
}

.lost-content {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: center;
  padding: 40px 60px;
  border-radius: 28px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.3);
  box-shadow: 0 30px 80px rgba(2, 6, 23, 0.6);
  backdrop-filter: blur(14px);
}

.astronaut {
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 6s ease-in-out infinite;
}

.astronaut svg {
  width: 260px;
  height: 260px;
}

.lost-text {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.signal {
  font-size: 12px;
  letter-spacing: 4px;
  color: rgba(148, 163, 184, 0.8);
}

.lost-text h1 {
  margin: 0;
  font-size: 46px;
  color: #f8fafc;
}

.lost-text p {
  margin: 0 0 20px;
  font-size: 16px;
  color: rgba(226, 232, 240, 0.85);
}

.return-button {
  align-self: flex-start;
  padding: 12px 26px;
  border-radius: 999px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #0f172a;
  box-shadow: 0 10px 30px rgba(148, 163, 184, 0.35);
}

.lost-code {
  position: absolute;
  bottom: 24px;
  right: 36px;
  font-size: 120px;
  font-weight: 700;
  color: rgba(148, 163, 184, 0.15);
  pointer-events: none;
}

@media (max-width: 1024px) {
  .lost-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .lost-text {
    align-items: center;
  }

  .return-button {
    align-self: center;
  }

  .lost-code {
    position: static;
    font-size: 72px;
    text-align: center;
  }
}

@media (max-width: 640px) {
  .lost-content {
    padding: 32px 24px;
  }

  .astronaut svg {
    width: 200px;
    height: 200px;
  }

  .lost-text h1 {
    font-size: 36px;
  }
}

@keyframes float {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-12px);
  }
  100% {
    transform: translateY(0);
  }
}

@keyframes stars-float {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-40px);
  }
}
</style>
