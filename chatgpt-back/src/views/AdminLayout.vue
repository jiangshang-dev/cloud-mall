<template>
  <div class="layout">
    <aside>
      <div class="logo">Whpayok Admin</div>
      <router-link to="/products">商品上架</router-link>
      <router-link to="/cdks">卡密库存</router-link>
      <router-link to="/delivery">交付任务</router-link>
    </aside>
    <main><router-view /></main>
  </div>
</template>
<script setup></script>
<style scoped>
.layout { display:flex; min-height:100vh; }
aside { width:200px; background:#0f172a; color:#fff; padding:20px 14px; display:flex; flex-direction:column; gap:8px; }
.logo { font-weight:700; margin-bottom:16px; }
aside a { color:#cbd5e1; padding:8px 10px; border-radius:8px; }
aside a.router-link-active { background:#1e293b; color:#fff; }
main { flex:1; padding:24px; }
</style>
