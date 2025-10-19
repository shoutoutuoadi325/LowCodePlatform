<template>
  <div class="system-config">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>系统配置</h2>
          <p>管理系统参数、MQTT配置和用户权限</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="saveAllConfigs" :loading="saving">
            <el-icon><Check /></el-icon>
            保存所有配置
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 配置菜单 -->
      <el-col :span="6">
        <el-card class="config-menu">
          <template #header>
            <span>配置分类</span>
          </template>
          
          <el-menu
            v-model="activeConfig"
            class="config-menu-list"
            @select="handleMenuSelect"
          >
            <el-menu-item index="mqtt">
              <el-icon><Connection /></el-icon>
              <span>MQTT配置</span>
            </el-menu-item>
            <el-menu-item index="system">
              <el-icon><Setting /></el-icon>
              <span>系统参数</span>
            </el-menu-item>
            <el-menu-item index="users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="logs">
              <el-icon><Document /></el-icon>
              <span>日志配置</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 配置内容 -->
      <el-col :span="18">
        <!-- MQTT配置 -->
        <el-card v-show="activeConfig === 'mqtt'" class="config-content">
          <template #header>
            <div class="config-header">
              <span>MQTT配置</span>
              <el-button @click="testMqttConnection" :loading="testing">
                测试连接
              </el-button>
            </div>
          </template>
          
          <el-form
            ref="mqttFormRef"
            :model="mqttConfig"
            :rules="mqttRules"
            label-width="120px"
            v-loading="loading"
          >
            <el-form-item label="服务器地址" prop="host">
              <el-input v-model="mqttConfig.host" placeholder="mqtt://localhost" />
            </el-form-item>
            
            <el-form-item label="端口" prop="port">
              <el-input-number
                v-model="mqttConfig.port"
                :min="1"
                :max="65535"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="用户名" prop="username">
              <el-input v-model="mqttConfig.username" />
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="mqttConfig.password"
                type="password"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="客户端ID" prop="clientId">
              <el-input v-model="mqttConfig.clientId" />
            </el-form-item>
            
            <el-form-item label="保持连接">
              <el-switch v-model="mqttConfig.keepAlive" />
            </el-form-item>
            
            <el-form-item label="清除会话">
              <el-switch v-model="mqttConfig.cleanSession" />
            </el-form-item>
            
            <el-form-item label="重连间隔(秒)">
              <el-input-number
                v-model="mqttConfig.reconnectPeriod"
                :min="1"
                :max="300"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 系统参数 -->
        <el-card v-show="activeConfig === 'system'" class="config-content">
          <template #header>
            <span>系统参数</span>
          </template>
          
          <el-form
            ref="systemFormRef"
            :model="systemConfig"
            :rules="systemRules"
            label-width="120px"
            v-loading="loading"
          >
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="systemConfig.systemName" />
            </el-form-item>
            
            <el-form-item label="系统版本" prop="version">
              <el-input v-model="systemConfig.version" readonly />
            </el-form-item>
            
            <el-form-item label="数据保留天数" prop="dataRetentionDays">
              <el-input-number
                v-model="systemConfig.dataRetentionDays"
                :min="1"
                :max="365"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="最大设备数量" prop="maxDevices">
              <el-input-number
                v-model="systemConfig.maxDevices"
                :min="1"
                :max="10000"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="API请求限制" prop="apiRateLimit">
              <el-input-number
                v-model="systemConfig.apiRateLimit"
                :min="10"
                :max="10000"
                style="width: 100%"
              />
              <div class="form-tip">每分钟最大请求数</div>
            </el-form-item>
            
            <el-form-item label="启用调试模式">
              <el-switch v-model="systemConfig.debugMode" />
            </el-form-item>
            
            <el-form-item label="自动备份">
              <el-switch v-model="systemConfig.autoBackup" />
            </el-form-item>
            
            <el-form-item label="备份间隔" v-if="systemConfig.autoBackup">
              <el-select v-model="systemConfig.backupInterval" style="width: 100%">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 用户管理 -->
        <el-card v-show="activeConfig === 'users'" class="config-content">
          <template #header>
            <div class="config-header">
              <span>用户管理</span>
              <el-button type="primary" @click="showUserDialog = true">
                <el-icon><Plus /></el-icon>
                添加用户
              </el-button>
            </div>
          </template>
          
          <div v-loading="loading">
            <el-table :data="users" style="width: 100%">
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="role" label="角色">
                <template #default="{ row }">
                  <el-tag :type="getRoleType(row.role)">
                    {{ getRoleText(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                    {{ row.status === 'active' ? '活跃' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastLogin" label="最后登录">
                <template #default="{ row }">
                  {{ formatDate(row.lastLogin) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" @click="editUser(row)">
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    :type="row.status === 'active' ? 'warning' : 'success'"
                    @click="toggleUserStatus(row)"
                  >
                    {{ row.status === 'active' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="deleteUser(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>

        <!-- 安全设置 -->
        <el-card v-show="activeConfig === 'security'" class="config-content">
          <template #header>
            <span>安全设置</span>
          </template>
          
          <el-form
            ref="securityFormRef"
            :model="securityConfig"
            label-width="120px"
            v-loading="loading"
          >
            <el-form-item label="密码最小长度">
              <el-input-number
                v-model="securityConfig.minPasswordLength"
                :min="6"
                :max="32"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="密码复杂度">
              <el-checkbox-group v-model="securityConfig.passwordComplexity">
                <el-checkbox label="uppercase">包含大写字母</el-checkbox>
                <el-checkbox label="lowercase">包含小写字母</el-checkbox>
                <el-checkbox label="numbers">包含数字</el-checkbox>
                <el-checkbox label="symbols">包含特殊字符</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="会话超时(分钟)">
              <el-input-number
                v-model="securityConfig.sessionTimeout"
                :min="5"
                :max="1440"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="最大登录尝试">
              <el-input-number
                v-model="securityConfig.maxLoginAttempts"
                :min="3"
                :max="10"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="锁定时间(分钟)">
              <el-input-number
                v-model="securityConfig.lockoutDuration"
                :min="5"
                :max="60"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="启用双因子认证">
              <el-switch v-model="securityConfig.enableTwoFactor" />
            </el-form-item>
            
            <el-form-item label="强制HTTPS">
              <el-switch v-model="securityConfig.forceHttps" />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 日志配置 -->
        <el-card v-show="activeConfig === 'logs'" class="config-content">
          <template #header>
            <span>日志配置</span>
          </template>
          
          <el-form
            ref="logsFormRef"
            :model="logsConfig"
            label-width="120px"
            v-loading="loading"
          >
            <el-form-item label="日志级别">
              <el-select v-model="logsConfig.level" style="width: 100%">
                <el-option label="DEBUG" value="debug" />
                <el-option label="INFO" value="info" />
                <el-option label="WARN" value="warn" />
                <el-option label="ERROR" value="error" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="日志保留天数">
              <el-input-number
                v-model="logsConfig.retentionDays"
                :min="1"
                :max="365"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="最大文件大小(MB)">
              <el-input-number
                v-model="logsConfig.maxFileSize"
                :min="1"
                :max="1024"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="启用访问日志">
              <el-switch v-model="logsConfig.enableAccessLog" />
            </el-form-item>
            
            <el-form-item label="启用错误日志">
              <el-switch v-model="logsConfig.enableErrorLog" />
            </el-form-item>
            
            <el-form-item label="启用审计日志">
              <el-switch v-model="logsConfig.enableAuditLog" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="showUserDialog"
      :title="editingUser ? '编辑用户' : '添加用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!!editingUser" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="操作员" value="operator" />
            <el-option label="观察者" value="viewer" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showUserDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemService } from '@/services/api'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const activeConfig = ref('mqtt')
const showUserDialog = ref(false)
const editingUser = ref(null)

// 表单引用
const mqttFormRef = ref()
const systemFormRef = ref()
const securityFormRef = ref()
const logsFormRef = ref()
const userFormRef = ref()

// MQTT配置
const mqttConfig = reactive({
  host: 'mqtt://localhost',
  port: 1883,
  username: '',
  password: '',
  clientId: 'lowcode-platform',
  keepAlive: true,
  cleanSession: true,
  reconnectPeriod: 30
})

// 系统配置
const systemConfig = reactive({
  systemName: '曦源IoT低代码平台',
  version: '1.0.0',
  dataRetentionDays: 30,
  maxDevices: 1000,
  apiRateLimit: 1000,
  debugMode: false,
  autoBackup: true,
  backupInterval: 'daily'
})

// 安全配置
const securityConfig = reactive({
  minPasswordLength: 8,
  passwordComplexity: ['lowercase', 'numbers'],
  sessionTimeout: 60,
  maxLoginAttempts: 5,
  lockoutDuration: 15,
  enableTwoFactor: false,
  forceHttps: false
})

// 日志配置
const logsConfig = reactive({
  level: 'info',
  retentionDays: 30,
  maxFileSize: 100,
  enableAccessLog: true,
  enableErrorLog: true,
  enableAuditLog: true
})

// 用户数据
const users = ref([])
const userForm = reactive({
  username: '',
  email: '',
  password: '',
  role: 'viewer'
})

// 表单验证规则
const mqttRules = {
  host: [{ required: true, message: '请输入服务器地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }]
}

const systemRules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }]
}

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 方法
const handleMenuSelect = (key) => {
  activeConfig.value = key
  if (key === 'users') {
    loadUsers()
  }
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const [mqttRes, systemRes, securityRes, logsRes] = await Promise.all([
      systemService.getMqttConfig(),
      systemService.getSystemConfig(),
      systemService.getSecurityConfig(),
      systemService.getLogsConfig()
    ])
    
    Object.assign(mqttConfig, mqttRes.data)
    Object.assign(systemConfig, systemRes.data)
    Object.assign(securityConfig, securityRes.data)
    Object.assign(logsConfig, logsRes.data)
  } catch (error) {
    ElMessage.error('加载配置失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await systemService.getUsers()
    users.value = response.data
  } catch (error) {
    ElMessage.error('加载用户列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const saveAllConfigs = async () => {
  saving.value = true
  try {
    await Promise.all([
      systemService.updateMqttConfig(mqttConfig),
      systemService.updateSystemConfig(systemConfig),
      systemService.updateSecurityConfig(securityConfig),
      systemService.updateLogsConfig(logsConfig)
    ])
    ElMessage.success('所有配置保存成功')
  } catch (error) {
    ElMessage.error('保存配置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const testMqttConnection = async () => {
  testing.value = true
  try {
    await systemService.testMqttConnection(mqttConfig)
    ElMessage.success('MQTT连接测试成功')
  } catch (error) {
    ElMessage.error('MQTT连接测试失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

const editUser = (user) => {
  editingUser.value = user
  Object.assign(userForm, {
    username: user.username,
    email: user.email,
    role: user.role,
    password: ''
  })
  showUserDialog.value = true
}

const saveUser = async () => {
  try {
    await userFormRef.value.validate()
    saving.value = true
    
    if (editingUser.value) {
      await systemService.updateUser(editingUser.value.id, userForm)
      ElMessage.success('用户更新成功')
    } else {
      await systemService.createUser(userForm)
      ElMessage.success('用户创建成功')
    }
    
    showUserDialog.value = false
    resetUserForm()
    loadUsers()
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存用户失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

const toggleUserStatus = async (user) => {
  try {
    const newStatus = user.status === 'active' ? 'inactive' : 'active'
    await systemService.updateUserStatus(user.id, newStatus)
    user.status = newStatus
    ElMessage.success(`用户已${newStatus === 'active' ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('更新用户状态失败: ' + error.message)
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await systemService.deleteUser(user.id)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败: ' + error.message)
    }
  }
}

const resetUserForm = () => {
  editingUser.value = null
  Object.assign(userForm, {
    username: '',
    email: '',
    password: '',
    role: 'viewer'
  })
}

const getRoleType = (role) => {
  const typeMap = {
    admin: 'danger',
    operator: 'warning',
    viewer: 'info'
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role) => {
  const textMap = {
    admin: '管理员',
    operator: '操作员',
    viewer: '观察者'
  }
  return textMap[role] || role
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.system-config {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0;
  color: #303133;
}

.config-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.config-menu {
  border-right: 1px solid #e4e7ed;
}

.config-panel {
  padding: 20px;
}

.form-section {
  margin-bottom: 30px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
}

.user-table {
  margin-top: 20px;
}

.status-tag {
  margin-right: 8px;
}
</style>