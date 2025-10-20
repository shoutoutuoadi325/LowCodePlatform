# 设备状态控制功能修复

## 问题描述
用户在前端设备管理页面无法直接控制设备的 ONLINE 或 OFFLINE 状态。

## 原因分析
原有的"控制设备"对话框只提供了三个操作选项：
- 开启 (turn_on)
- 关闭 (turn_off)  
- 设置状态 (set_state) - 用于设置设备工作状态，如亮度、温度等

设备的连接状态（ONLINE/OFFLINE/DISABLED）需要通过"编辑设备"功能来修改，但这对用户来说不够直观。

## 解决方案
在"控制设备"对话框中新增"修改设备状态"选项，允许用户直接在控制对话框中修改设备的连接状态。

## 修改内容

### 1. 前端 UI 修改 (`frontend/src/views/DeviceManagement.vue`)

#### 添加新的操作选项
在控制对话框的操作单选组中添加"修改设备状态"选项：
```vue
<el-form-item label="操作">
  <el-radio-group v-model="controlAction">
    <el-radio label="turn_on">开启</el-radio>
    <el-radio label="turn_off">关闭</el-radio>
    <el-radio label="set_state">设置状态</el-radio>
    <el-radio label="change_status">修改设备状态</el-radio>  <!-- 新增 -->
  </el-radio-group>
</el-form-item>
```

#### 添加状态选择器
当选择"修改设备状态"时，显示状态选择下拉框：
```vue
<template v-if="controlAction === 'change_status'">
  <el-form-item label="设置状态">
    <el-select v-model="deviceStatusChange" placeholder="选择设备状态">
      <el-option label="在线" value="ONLINE" />
      <el-option label="离线" value="OFFLINE" />
      <el-option label="禁用" value="DISABLED" />
    </el-select>
  </el-form-item>
</template>
```

### 2. 前端逻辑修改

#### 添加响应式变量
```javascript
const deviceStatusChange = ref('ONLINE')
```

#### 初始化设备状态
在 `controlDevice` 函数中初始化当前设备的状态：
```javascript
const controlDevice = (device) => {
  currentDevice.value = device
  controlAction.value = 'turn_on'
  controlParams.value = ''
  lightBrightness.value = 100
  lightColor.value = '#FFFFFF'
  deviceStatusChange.value = device.status || 'ONLINE'  // 新增：初始化为当前状态
  showControlDialog.value = true
}
```

#### 处理状态更改
在 `executeControl` 函数开头添加状态更改处理逻辑：
```javascript
const executeControl = async () => {
  try {
    // Handle device status change
    if (controlAction.value === 'change_status') {
      const updateData = {
        ...currentDevice.value,
        status: deviceStatusChange.value
      }
      await deviceService.update(currentDevice.value.id, updateData)
      ElMessage.success('设备状态更新成功')
      showControlDialog.value = false
      loadDevices()
      return
    }
    
    // ... 原有的控制逻辑
  }
}
```

## 使用说明

### 修改设备状态
1. 在设备管理页面，点击设备行的"控制"按钮
2. 在弹出的对话框中，选择"修改设备状态"单选按钮
3. 从下拉菜单中选择目标状态：
   - **在线 (ONLINE)**: 设备在线且可正常控制
   - **离线 (OFFLINE)**: 设备离线，无法控制
   - **禁用 (DISABLED)**: 设备被禁用，无法控制
4. 点击"执行"按钮完成状态修改

### 设备状态说明
- **ONLINE**: 设备可以接收并执行控制命令
- **OFFLINE**: 设备不可用，所有控制命令都会失败
- **DISABLED**: 设备被管理员禁用，所有控制命令都会失败

## 后端支持
后端 `DeviceService.updateDevice()` 方法已支持更新设备状态，无需修改。

## 测试建议
1. 测试将设备从 ONLINE 改为 OFFLINE
2. 测试将设备从 OFFLINE 改为 ONLINE
3. 测试将设备改为 DISABLED 状态
4. 验证状态改变后设备列表实时刷新
5. 验证 OFFLINE/DISABLED 状态的设备无法执行控制命令

## 相关文件
- `frontend/src/views/DeviceManagement.vue` - 设备管理页面
- `backend/device-service/src/main/java/com/xiyuan/iot/device/service/DeviceService.java` - 设备服务
- `backend/device-service/src/main/java/com/xiyuan/iot/device/controller/DeviceController.java` - 设备控制器
