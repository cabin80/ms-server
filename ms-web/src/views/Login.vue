<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">♪</div>
      <h2>{{ isRegister ? '注册' : '登录' }}</h2>
      <div class="form-group">
        <input v-model="username" type="text" placeholder="用户名" @keyup.enter="submit" />
      </div>
      <div class="form-group">
        <input v-model="password" type="password" placeholder="密码" @keyup.enter="submit" />
      </div>
      <div v-if="isRegister" class="form-group">
        <input v-model="nickname" type="text" placeholder="昵称" @keyup.enter="submit" />
      </div>
      <div class="error-msg" v-if="error">{{ error }}</div>
      <button class="submit-btn" @click="submit" :disabled="loading">
        {{ loading ? '处理中...' : (isRegister ? '注册' : '登录') }}
      </button>
      <div class="switch" @click="isRegister = !isRegister">
        {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
      </div>
      <button class="back-btn" @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      username: 'demo',
      password: '123456',
      nickname: '',
      isRegister: false,
      error: '',
      loading: false
    }
  },
  methods: {
    async submit() {
      if (!this.username || !this.password) {
        this.error = '请填写完整'
        return
      }
      this.loading = true
      this.error = ''
      try {
        if (this.isRegister) {
          await this.$store.dispatch('login', { username: this.username, password: this.password })
          // Register first then auto-login would be better
          const res = await this.$store.dispatch('login', { username: this.username, password: this.password })
          if (res.code === 200) {
            this.$router.push('/')
          } else {
            this.error = res.message || '注册失败'
          }
        } else {
          const res = await this.$store.dispatch('login', { username: this.username, password: this.password })
          if (res.code === 200) {
            this.$router.push('/')
          } else {
            this.error = res.message || '登录失败'
          }
        }
      } catch(e) {
        this.error = '操作失败，请重试'
      }
      this.loading = false
    }
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 20px;
  padding: 48px 40px;
  width: 360px;
  text-align: center;
}
.login-logo {
  font-size: 48px;
  color: #7a9aff;
  text-shadow: 0 0 40px rgba(122,154,255,0.4);
  margin-bottom: 12px;
}
h2 {
  color: #fff;
  font-size: 22px;
  margin-bottom: 24px;
}
.form-group {
  margin-bottom: 16px;
}
.form-group input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  color: #e0e0e0;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s;
}
.form-group input:focus {
  border-color: #7a9aff;
  box-shadow: 0 0 20px rgba(122,154,255,0.1);
}
.form-group input::placeholder { color: #555; }
.error-msg {
  color: #ff6b6b;
  font-size: 13px;
  margin-bottom: 12px;
}
.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #7a9aff, #302b63);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.submit-btn:hover { opacity: 0.9; transform: translateY(-1px); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.switch {
  margin-top: 16px;
  color: #7a9aff;
  font-size: 13px;
  cursor: pointer;
}
.switch:hover { text-decoration: underline; }
.back-btn {
  margin-top: 12px;
  background: none;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 8px;
  color: #6a6a8a;
  padding: 8px 20px;
  cursor: pointer;
  font-family: inherit;
  font-size: 12px;
}
.back-btn:hover { color: #7a9aff; }
</style>
