<template>
  <div class="layout">
    <aside class="side">
      <div class="logo"><span class="mark">W</span> Whpayok</div>
      <nav>
        <router-link class="nav-item" to="/shop" active-class="active">订阅</router-link>
        <router-link class="nav-item" to="/orders" active-class="active">我的订单</router-link>
        <a class="nav-item">我的邀请</a>
        <a class="nav-item">个人中心</a>
      </nav>
    </aside>
    <main class="content">
      <header class="top">
        <div class="brand">Whpayok</div>
        <div class="user">demo</div>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup></script>

<style scoped>
.layout { min-height:100vh; display:flex; }
.side { width:220px; background:#fff; border-right:1px solid #eef2f7; padding:18px 12px; }
.logo { display:flex; align-items:center; gap:10px; font-weight:700; padding:8px 10px 20px; }
.mark { width:28px; height:28px; border-radius:8px; background:#2563eb; color:#fff; display:inline-flex; align-items:center; justify-content:center; }
.nav-item { display:block; padding:10px 12px; border-radius:8px; color:#334155; margin-bottom:4px; }
.nav-item.active { background:#f1f5f9; border-left:3px solid #2563eb; font-weight:600; }
.content { flex:1; min-width:0; }
.top { height:56px; background:#fff; border-bottom:1px solid #eef2f7; display:flex; align-items:center; justify-content:space-between; padding:0 24px; }
.user { color:#64748b; }
@media (max-width:860px){ .side{display:none;} }
</style>
