<template>
  <el-config-provider :empty-values="[undefined, null, '']" :value-on-clear="() => undefined">
    <router-view />
  </el-config-provider>
</template>
