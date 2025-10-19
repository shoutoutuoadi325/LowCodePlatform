#!/usr/bin/env node

/**
 * 测试数据生成脚本
 * 为LowCodePlatform生成完整的模拟数据
 */

const axios = require('axios');

// 服务配置
const DEVICE_SERVICE_URL = process.env.DEVICE_SERVICE_URL || 'http://localhost:8081';
const SCENE_SERVICE_URL = process.env.SCENE_SERVICE_URL || 'http://localhost:8082';

// 创建API客户端
const deviceApi = axios.create({
  baseURL: DEVICE_SERVICE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

const sceneApi = axios.create({
  baseURL: SCENE_SERVICE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 设备类型模拟数据
const deviceTypeModels = [
  {
    typeIdentifier: 'smart_bulb',
    typeName: '智能灯泡',
    description: '可调光调色的智能LED灯泡',
    category: '照明设备',
    manufacturer: 'SmartHome Inc.',
    model: 'SH-BULB-001',
    version: '1.0.0',
    properties: [
      {
        propertyKey: 'brightness',
        propertyName: '亮度',
        dataType: 'INTEGER',
        unit: '%',
        minValue: 0,
        maxValue: 100,
        defaultValue: '50',
        isReadonly: false,
        description: '灯泡亮度百分比'
      },
      {
        propertyKey: 'color',
        propertyName: '颜色',
        dataType: 'STRING',
        defaultValue: '#FFFFFF',
        isReadonly: false,
        description: '灯泡颜色（十六进制）'
      },
      {
        propertyKey: 'power_state',
        propertyName: '电源状态',
        dataType: 'BOOLEAN',
        defaultValue: 'false',
        isReadonly: false,
        description: '灯泡开关状态'
      }
    ],
    operations: [
      {
        operationKey: 'turn_on',
        operationName: '开灯',
        description: '打开灯泡',
        parameters: []
      },
      {
        operationKey: 'turn_off',
        operationName: '关灯',
        description: '关闭灯泡',
        parameters: []
      },
      {
        operationKey: 'set_brightness',
        operationName: '设置亮度',
        description: '设置灯泡亮度',
        parameters: [
          {
            parameterKey: 'brightness',
            parameterName: '亮度值',
            dataType: 'INTEGER',
            isRequired: true,
            minValue: 0,
            maxValue: 100
          }
        ]
      }
    ],
    events: [
      {
        eventKey: 'state_changed',
        eventName: '状态变化',
        description: '灯泡状态发生变化时触发',
        eventData: [
          {
            dataKey: 'old_state',
            dataName: '旧状态',
            dataType: 'STRING'
          },
          {
            dataKey: 'new_state',
            dataName: '新状态',
            dataType: 'STRING'
          }
        ]
      }
    ]
  },
  {
    typeIdentifier: 'temperature_sensor',
    typeName: '温度传感器',
    description: '高精度数字温度传感器',
    category: '传感器',
    manufacturer: 'SensorTech Ltd.',
    model: 'ST-TEMP-002',
    version: '2.1.0',
    properties: [
      {
        propertyKey: 'temperature',
        propertyName: '温度',
        dataType: 'FLOAT',
        unit: '°C',
        minValue: -40,
        maxValue: 85,
        defaultValue: '25.0',
        isReadonly: true,
        description: '当前环境温度'
      },
      {
        propertyKey: 'humidity',
        propertyName: '湿度',
        dataType: 'FLOAT',
        unit: '%',
        minValue: 0,
        maxValue: 100,
        defaultValue: '50.0',
        isReadonly: true,
        description: '当前环境湿度'
      },
      {
        propertyKey: 'battery_level',
        propertyName: '电池电量',
        dataType: 'INTEGER',
        unit: '%',
        minValue: 0,
        maxValue: 100,
        defaultValue: '100',
        isReadonly: true,
        description: '传感器电池电量'
      }
    ],
    operations: [
      {
        operationKey: 'calibrate',
        operationName: '校准',
        description: '校准传感器',
        parameters: []
      }
    ],
    events: [
      {
        eventKey: 'temperature_alert',
        eventName: '温度报警',
        description: '温度超出正常范围时触发',
        eventData: [
          {
            dataKey: 'temperature',
            dataName: '当前温度',
            dataType: 'FLOAT'
          },
          {
            dataKey: 'threshold',
            dataName: '阈值',
            dataType: 'FLOAT'
          }
        ]
      }
    ]
  },
  {
    typeIdentifier: 'smart_lock',
    typeName: '智能门锁',
    description: '指纹识别智能门锁',
    category: '安防设备',
    manufacturer: 'SecureTech Co.',
    model: 'SEC-LOCK-003',
    version: '1.5.0',
    properties: [
      {
        propertyKey: 'lock_state',
        propertyName: '锁定状态',
        dataType: 'BOOLEAN',
        defaultValue: 'true',
        isReadonly: false,
        description: '门锁是否锁定'
      },
      {
        propertyKey: 'battery_level',
        propertyName: '电池电量',
        dataType: 'INTEGER',
        unit: '%',
        minValue: 0,
        maxValue: 100,
        defaultValue: '85',
        isReadonly: true,
        description: '门锁电池电量'
      }
    ],
    operations: [
      {
        operationKey: 'lock',
        operationName: '上锁',
        description: '锁定门锁',
        parameters: []
      },
      {
        operationKey: 'unlock',
        operationName: '解锁',
        description: '解锁门锁',
        parameters: [
          {
            parameterKey: 'unlock_method',
            parameterName: '解锁方式',
            dataType: 'STRING',
            isRequired: true,
            description: '指纹/密码/钥匙'
          }
        ]
      }
    ],
    events: [
      {
        eventKey: 'unlock_attempt',
        eventName: '解锁尝试',
        description: '有人尝试解锁时触发',
        eventData: [
          {
            dataKey: 'method',
            dataName: '解锁方式',
            dataType: 'STRING'
          },
          {
            dataKey: 'success',
            dataName: '是否成功',
            dataType: 'BOOLEAN'
          }
        ]
      }
    ]
  },
  {
    typeIdentifier: 'air_conditioner',
    typeName: '智能空调',
    description: '变频智能空调',
    category: '环境控制',
    manufacturer: 'CoolAir Corp.',
    model: 'CA-AC-004',
    version: '3.0.0',
    properties: [
      {
        propertyKey: 'power_state',
        propertyName: '电源状态',
        dataType: 'BOOLEAN',
        defaultValue: 'false',
        isReadonly: false,
        description: '空调开关状态'
      },
      {
        propertyKey: 'target_temperature',
        propertyName: '目标温度',
        dataType: 'INTEGER',
        unit: '°C',
        minValue: 16,
        maxValue: 30,
        defaultValue: '24',
        isReadonly: false,
        description: '设定温度'
      },
      {
        propertyKey: 'current_temperature',
        propertyName: '当前温度',
        dataType: 'FLOAT',
        unit: '°C',
        defaultValue: '25.5',
        isReadonly: true,
        description: '室内当前温度'
      },
      {
        propertyKey: 'mode',
        propertyName: '工作模式',
        dataType: 'STRING',
        defaultValue: 'auto',
        isReadonly: false,
        description: '制冷/制热/除湿/送风/自动'
      }
    ],
    operations: [
      {
        operationKey: 'turn_on',
        operationName: '开机',
        description: '打开空调',
        parameters: []
      },
      {
        operationKey: 'turn_off',
        operationName: '关机',
        description: '关闭空调',
        parameters: []
      },
      {
        operationKey: 'set_temperature',
        operationName: '设置温度',
        description: '设置目标温度',
        parameters: [
          {
            parameterKey: 'temperature',
            parameterName: '目标温度',
            dataType: 'INTEGER',
            isRequired: true,
            minValue: 16,
            maxValue: 30
          }
        ]
      }
    ],
    events: [
      {
        eventKey: 'temperature_reached',
        eventName: '温度达标',
        description: '室温达到设定温度时触发',
        eventData: [
          {
            dataKey: 'target_temp',
            dataName: '目标温度',
            dataType: 'INTEGER'
          },
          {
            dataKey: 'current_temp',
            dataName: '当前温度',
            dataType: 'FLOAT'
          }
        ]
      }
    ]
  },
  {
    typeIdentifier: 'security_camera',
    typeName: '安防摄像头',
    description: '高清网络安防摄像头',
    category: '安防设备',
    manufacturer: 'VisionSec Ltd.',
    model: 'VS-CAM-005',
    version: '2.3.0',
    properties: [
      {
        propertyKey: 'recording_state',
        propertyName: '录制状态',
        dataType: 'BOOLEAN',
        defaultValue: 'true',
        isReadonly: false,
        description: '是否正在录制'
      },
      {
        propertyKey: 'motion_detection',
        propertyName: '移动检测',
        dataType: 'BOOLEAN',
        defaultValue: 'true',
        isReadonly: false,
        description: '是否启用移动检测'
      },
      {
        propertyKey: 'resolution',
        propertyName: '分辨率',
        dataType: 'STRING',
        defaultValue: '1080p',
        isReadonly: false,
        description: '录制分辨率'
      }
    ],
    operations: [
      {
        operationKey: 'start_recording',
        operationName: '开始录制',
        description: '开始视频录制',
        parameters: []
      },
      {
        operationKey: 'stop_recording',
        operationName: '停止录制',
        description: '停止视频录制',
        parameters: []
      },
      {
        operationKey: 'take_snapshot',
        operationName: '拍照',
        description: '拍摄当前画面',
        parameters: []
      }
    ],
    events: [
      {
        eventKey: 'motion_detected',
        eventName: '检测到移动',
        description: '摄像头检测到移动物体时触发',
        eventData: [
          {
            dataKey: 'confidence',
            dataName: '置信度',
            dataType: 'FLOAT'
          },
          {
            dataKey: 'timestamp',
            dataName: '时间戳',
            dataType: 'STRING'
          }
        ]
      }
    ]
  }
];

// 设备实例模拟数据
const devices = [
  {
    deviceId: 'bulb_living_001',
    deviceName: '客厅主灯',
    deviceType: 'smart_bulb',
    location: '客厅',
    building: 'A栋',
    floor: '1楼',
    room: '客厅',
    status: 'ONLINE',
    description: '客厅中央吸顶灯'
  },
  {
    deviceId: 'bulb_bedroom_001',
    deviceName: '卧室台灯',
    deviceType: 'smart_bulb',
    location: '主卧',
    building: 'A栋',
    floor: '2楼',
    room: '主卧',
    status: 'ONLINE',
    description: '床头智能台灯'
  },
  {
    deviceId: 'temp_living_001',
    deviceName: '客厅温度传感器',
    deviceType: 'temperature_sensor',
    location: '客厅',
    building: 'A栋',
    floor: '1楼',
    room: '客厅',
    status: 'ONLINE',
    description: '监测客厅温湿度'
  },
  {
    deviceId: 'temp_bedroom_001',
    deviceName: '卧室温度传感器',
    deviceType: 'temperature_sensor',
    location: '主卧',
    building: 'A栋',
    floor: '2楼',
    room: '主卧',
    status: 'ONLINE',
    description: '监测卧室温湿度'
  },
  {
    deviceId: 'lock_main_001',
    deviceName: '大门智能锁',
    deviceType: 'smart_lock',
    location: '入口',
    building: 'A栋',
    floor: '1楼',
    room: '入口',
    status: 'ONLINE',
    description: '主入口门锁'
  },
  {
    deviceId: 'ac_living_001',
    deviceName: '客厅空调',
    deviceType: 'air_conditioner',
    location: '客厅',
    building: 'A栋',
    floor: '1楼',
    room: '客厅',
    status: 'ONLINE',
    description: '客厅中央空调'
  },
  {
    deviceId: 'ac_bedroom_001',
    deviceName: '卧室空调',
    deviceType: 'air_conditioner',
    location: '主卧',
    building: 'A栋',
    floor: '2楼',
    room: '主卧',
    status: 'ONLINE',
    description: '主卧空调'
  },
  {
    deviceId: 'cam_entrance_001',
    deviceName: '入口监控',
    deviceType: 'security_camera',
    location: '入口',
    building: 'A栋',
    floor: '1楼',
    room: '入口',
    status: 'ONLINE',
    description: '大门入口监控摄像头'
  },
  {
    deviceId: 'cam_living_001',
    deviceName: '客厅监控',
    deviceType: 'security_camera',
    location: '客厅',
    building: 'A栋',
    floor: '1楼',
    room: '客厅',
    status: 'ONLINE',
    description: '客厅安防监控'
  }
];

// 场景模拟数据
const scenes = [
  {
    sceneId: 'scene_home_001',
    name: '回家模式',
    description: '回家时自动执行的场景',
    status: 'ACTIVE',
    triggers: [
      {
        triggerType: 'DEVICE_STATE',
        deviceId: 'lock_main_001',
        property: 'lock_state',
        operator: 'EQUALS',
        value: 'false',
        description: '大门解锁时触发'
      }
    ],
    actions: [
      {
        deviceId: 'bulb_living_001',
        action: 'turn_on',
        parameters: { brightness: 80 },
        description: '打开客厅灯'
      },
      {
        deviceId: 'ac_living_001',
        action: 'turn_on',
        parameters: { temperature: 24 },
        description: '打开客厅空调'
      }
    ]
  },
  {
    sceneId: 'scene_sleep_001',
    name: '睡眠模式',
    description: '睡觉时自动执行的场景',
    status: 'ACTIVE',
    triggers: [
      {
        triggerType: 'TIME',
        cronExpression: '0 0 22 * * ?',
        description: '每天晚上10点触发'
      }
    ],
    actions: [
      {
        deviceId: 'bulb_living_001',
        action: 'turn_off',
        parameters: {},
        description: '关闭客厅灯'
      },
      {
        deviceId: 'bulb_bedroom_001',
        action: 'set_brightness',
        parameters: { brightness: 20 },
        description: '调暗卧室灯'
      },
      {
        deviceId: 'ac_bedroom_001',
        action: 'set_temperature',
        parameters: { temperature: 22 },
        description: '调整卧室空调温度'
      }
    ]
  },
  {
    sceneId: 'scene_away_001',
    name: '离家模式',
    description: '离家时自动执行的场景',
    status: 'ACTIVE',
    triggers: [
      {
        triggerType: 'DEVICE_STATE',
        deviceId: 'lock_main_001',
        property: 'lock_state',
        operator: 'EQUALS',
        value: 'true',
        description: '大门上锁时触发'
      }
    ],
    actions: [
      {
        deviceId: 'bulb_living_001',
        action: 'turn_off',
        parameters: {},
        description: '关闭客厅灯'
      },
      {
        deviceId: 'bulb_bedroom_001',
        action: 'turn_off',
        parameters: {},
        description: '关闭卧室灯'
      },
      {
        deviceId: 'ac_living_001',
        action: 'turn_off',
        parameters: {},
        description: '关闭客厅空调'
      },
      {
        deviceId: 'ac_bedroom_001',
        action: 'turn_off',
        parameters: {},
        description: '关闭卧室空调'
      },
      {
        deviceId: 'cam_entrance_001',
        action: 'start_recording',
        parameters: {},
        description: '开启入口监控录制'
      }
    ]
  },
  {
    sceneId: 'scene_security_001',
    name: '安防模式',
    description: '夜间安防场景',
    status: 'ACTIVE',
    triggers: [
      {
        triggerType: 'TIME',
        cronExpression: '0 0 23 * * ?',
        description: '每天晚上11点触发'
      }
    ],
    actions: [
      {
        deviceId: 'cam_entrance_001',
        action: 'start_recording',
        parameters: {},
        description: '开启入口监控'
      },
      {
        deviceId: 'cam_living_001',
        action: 'start_recording',
        parameters: {},
        description: '开启客厅监控'
      },
      {
        deviceId: 'lock_main_001',
        action: 'lock',
        parameters: {},
        description: '确保大门上锁'
      }
    ]
  }
];

// 工具函数
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function createDeviceType(deviceType) {
  try {
    console.log(`创建设备类型: ${deviceType.typeName}`);
    const response = await deviceApi.post('/api/device-types', deviceType);
    console.log(`✓ 设备类型 ${deviceType.typeName} 创建成功`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 409) {
      console.log(`⚠ 设备类型 ${deviceType.typeName} 已存在`);
      return null;
    }
    console.error(`✗ 创建设备类型 ${deviceType.typeName} 失败:`, error.message);
    throw error;
  }
}

async function createDevice(device) {
  try {
    console.log(`创建设备: ${device.deviceName}`);
    const response = await deviceApi.post('/api/devices', device);
    console.log(`✓ 设备 ${device.deviceName} 创建成功`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 409) {
      console.log(`⚠ 设备 ${device.deviceName} 已存在`);
      return null;
    }
    console.error(`✗ 创建设备 ${device.deviceName} 失败:`, error.message);
    throw error;
  }
}

async function createScene(scene) {
  try {
    console.log(`创建场景: ${scene.name}`);
    const response = await sceneApi.post('/api/scenes', scene);
    console.log(`✓ 场景 ${scene.name} 创建成功`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 409) {
      console.log(`⚠ 场景 ${scene.name} 已存在`);
      return null;
    }
    console.error(`✗ 创建场景 ${scene.name} 失败:`, error.message);
    throw error;
  }
}

async function checkServices() {
  console.log('检查服务状态...');
  
  try {
    await deviceApi.get('/api/device-types/test');
    console.log('✓ 设备服务连接正常');
  } catch (error) {
    console.error('✗ 设备服务连接失败:', error.message);
    throw new Error('设备服务不可用');
  }
  
  try {
    await sceneApi.get('/api/scenes');
    console.log('✓ 场景服务连接正常');
  } catch (error) {
    console.error('✗ 场景服务连接失败:', error.message);
    throw new Error('场景服务不可用');
  }
}

async function generateTestData() {
  console.log('🚀 开始生成测试数据...\n');
  
  try {
    // 检查服务状态
    await checkServices();
    console.log('');
    
    // 创建设备类型
    console.log('📋 创建设备类型...');
    for (const deviceType of deviceTypeModels) {
      await createDeviceType(deviceType);
      await delay(500); // 避免请求过快
    }
    console.log('');
    
    // 创建设备实例
    console.log('🔌 创建设备实例...');
    for (const device of devices) {
      await createDevice(device);
      await delay(500);
    }
    console.log('');
    
    // 创建场景
    console.log('🎬 创建场景...');
    for (const scene of scenes) {
      await createScene(scene);
      await delay(500);
    }
    console.log('');
    
    console.log('🎉 测试数据生成完成！');
    console.log(`
📊 数据统计:
- 设备类型: ${deviceTypeModels.length} 个
- 设备实例: ${devices.length} 个  
- 场景配置: ${scenes.length} 个

🌐 访问地址:
- 前端界面: http://localhost:8083
- 设备服务: ${DEVICE_SERVICE_URL}
- 场景服务: ${SCENE_SERVICE_URL}
    `);
    
  } catch (error) {
    console.error('❌ 生成测试数据失败:', error.message);
    process.exit(1);
  }
}

// 主函数
if (require.main === module) {
  generateTestData();
}

module.exports = {
  generateTestData,
  deviceTypeModels,
  devices,
  scenes
};