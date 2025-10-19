<template>
  <el-config-provider :locale="zhCn">
    <div id="app">
      <el-container>
        <el-header class="app-header">
          <div class="header-content">
            <h1>曦源 IoT 低代码平台</h1>
            <el-menu
              mode="horizontal"
              :default-active="$route.path"
              router
              class="nav-menu"
            >
              <el-menu-item index="/">
                <el-icon><HomeFilled /></el-icon>
                <span>仪表板</span>
              </el-menu-item>
              <el-menu-item index="/devices">
                <el-icon><Monitor /></el-icon>
                <span>设备管理</span>
              </el-menu-item>
              <el-menu-item index="/device-types">
                <el-icon><Grid /></el-icon>
                <span>设备类型</span>
              </el-menu-item>
              <el-menu-item index="/device-monitor">
                <el-icon><DataLine /></el-icon>
                <span>设备监控</span>
              </el-menu-item>
              <el-menu-item index="/scenes">
                <el-icon><Switch /></el-icon>
                <span>场景管理</span>
              </el-menu-item>
              <el-menu-item index="/scene-designer">
                <el-icon><MagicStick /></el-icon>
                <span>场景编排</span>
              </el-menu-item>
              <el-menu-item index="/data-analysis">
                <el-icon><TrendCharts /></el-icon>
                <span>数据分析</span>
              </el-menu-item>
              <el-menu-item index="/system-config">
                <el-icon><Setting /></el-icon>
                <span>系统配置</span>
              </el-menu-item>
              <el-menu-item index="/simulator-management">
                <el-icon><Cpu /></el-icon>
                <span>模拟器管理</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-header>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 
               'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
}

.el-container {
  height: 100%;
}

.app-header {
  background-color: #545c64;
  color: white;
  padding: 0;
  line-height: 60px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
}

.header-content h1 {
  font-size: 24px;
  margin-right: 50px;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  background-color: transparent !important;
  border: none !important;
}

.nav-menu .el-menu-item {
  color: white !important;
  border-bottom: 2px solid transparent !important;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-bottom-color: white !important;
}

.app-main {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
