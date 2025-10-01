<template>
  <div id="app">
    <nav class="navbar">
      <div class="navbar-brand">
        <h1>曦源 - 教学楼物联网管理平台</h1>
      </div>
      <div class="navbar-menu">
        <router-link to="/" class="navbar-item">设备管理</router-link>
        <router-link to="/scenes" class="navbar-item">场景编排</router-link>
      </div>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-brand h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.navbar-menu {
  display: flex;
  gap: 1.5rem;
}

.navbar-item {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.navbar-item:hover,
.navbar-item.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
}

.main-content {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}
</style>
