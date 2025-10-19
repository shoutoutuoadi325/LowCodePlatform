#!/usr/bin/env node

/**
 * 快速启动脚本
 * 一键启动LowCodePlatform并生成测试数据
 */

const { spawn, exec } = require('child_process');
const path = require('path');
const fs = require('fs');

// 项目根目录
const PROJECT_ROOT = path.resolve(__dirname, '..');

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
};

function colorLog(color, message) {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function execCommand(command, cwd = PROJECT_ROOT) {
  return new Promise((resolve, reject) => {
    exec(command, { cwd }, (error, stdout, stderr) => {
      if (error) {
        reject(error);
      } else {
        resolve({ stdout, stderr });
      }
    });
  });
}

async function checkPrerequisites() {
  colorLog('cyan', '🔍 检查环境依赖...');
  
  try {
    // 检查 Node.js
    const nodeVersion = await execCommand('node --version');
    colorLog('green', `✓ Node.js: ${nodeVersion.stdout.trim()}`);
    
    // 检查 Docker
    const dockerVersion = await execCommand('docker --version');
    colorLog('green', `✓ Docker: ${dockerVersion.stdout.trim()}`);
    
    // 检查 Docker Compose
    const composeVersion = await execCommand('docker-compose --version');
    colorLog('green', `✓ Docker Compose: ${composeVersion.stdout.trim()}`);
    
    return true;
  } catch (error) {
    colorLog('red', `✗ 环境检查失败: ${error.message}`);
    colorLog('yellow', '请确保已安装 Node.js、Docker 和 Docker Compose');
    return false;
  }
}

async function checkPorts() {
  colorLog('cyan', '🔍 检查端口占用...');
  
  const ports = [8080, 8081, 8082, 8083, 1883, 9001];
  const busyPorts = [];
  
  for (const port of ports) {
    try {
      if (process.platform === 'win32') {
        const result = await execCommand(`netstat -an | findstr :${port}`);
        if (result.stdout.trim()) {
          busyPorts.push(port);
        }
      } else {
        const result = await execCommand(`lsof -i :${port}`);
        if (result.stdout.trim()) {
          busyPorts.push(port);
        }
      }
    } catch (error) {
      // 端口未被占用
    }
  }
  
  if (busyPorts.length > 0) {
    colorLog('yellow', `⚠ 以下端口被占用: ${busyPorts.join(', ')}`);
    colorLog('yellow', '这可能会导致服务启动失败，请手动释放这些端口或停止相关服务');
    return false;
  } else {
    colorLog('green', '✓ 所有必需端口都可用');
    return true;
  }
}

async function installDependencies() {
  colorLog('cyan', '📦 安装项目依赖...');
  
  try {
    // 检查前端依赖
    const frontendPath = path.join(PROJECT_ROOT, 'frontend');
    const nodeModulesPath = path.join(frontendPath, 'node_modules');
    
    if (!fs.existsSync(nodeModulesPath)) {
      colorLog('yellow', '正在安装前端依赖...');
      await execCommand('npm install', frontendPath);
      colorLog('green', '✓ 前端依赖安装完成');
    } else {
      colorLog('green', '✓ 前端依赖已存在');
    }
    
    // 检查脚本依赖
    const packageJsonPath = path.join(PROJECT_ROOT, 'package.json');
    if (fs.existsSync(packageJsonPath)) {
      const rootNodeModulesPath = path.join(PROJECT_ROOT, 'node_modules');
      if (!fs.existsSync(rootNodeModulesPath)) {
        colorLog('yellow', '正在安装根目录依赖...');
        await execCommand('npm install');
        colorLog('green', '✓ 根目录依赖安装完成');
      } else {
        colorLog('green', '✓ 根目录依赖已存在');
      }
    }
    
    return true;
  } catch (error) {
    colorLog('red', `✗ 依赖安装失败: ${error.message}`);
    return false;
  }
}

async function startServices() {
  colorLog('cyan', '🚀 启动后端服务...');
  
  try {
    // 停止可能存在的旧容器
    try {
      await execCommand('docker-compose down');
    } catch (error) {
      // 忽略错误，可能是首次运行
    }
    
    // 启动服务
    colorLog('yellow', '正在启动 Docker 容器...');
    await execCommand('docker-compose up -d');
    
    colorLog('green', '✓ 后端服务启动完成');
    
    // 等待服务就绪
    colorLog('yellow', '等待服务就绪...');
    await delay(30000); // 等待30秒
    
    return true;
  } catch (error) {
    colorLog('red', `✗ 服务启动失败: ${error.message}`);
    return false;
  }
}

async function checkServiceHealth() {
  colorLog('cyan', '🏥 检查服务健康状态...');
  
  const services = [
    { name: '设备服务', url: 'http://localhost:8081/api/device-types/test' },
    { name: '场景服务', url: 'http://localhost:8082/api/scenes' }
  ];
  
  for (const service of services) {
    let retries = 5;
    let healthy = false;
    
    while (retries > 0 && !healthy) {
      try {
        await execCommand(`curl -s ${service.url}`);
        colorLog('green', `✓ ${service.name} 健康检查通过`);
        healthy = true;
      } catch (error) {
        retries--;
        if (retries > 0) {
          colorLog('yellow', `⏳ ${service.name} 未就绪，等待中... (剩余重试: ${retries})`);
          await delay(5000);
        } else {
          colorLog('red', `✗ ${service.name} 健康检查失败`);
        }
      }
    }
    
    if (!healthy) {
      return false;
    }
  }
  
  return true;
}

async function generateTestData() {
  colorLog('cyan', '📊 生成测试数据...');
  
  try {
    const dataScriptPath = path.join(__dirname, 'generate-test-data.js');
    
    if (!fs.existsSync(dataScriptPath)) {
      colorLog('red', '✗ 数据生成脚本不存在');
      return false;
    }
    
    await execCommand(`node "${dataScriptPath}"`);
    colorLog('green', '✓ 测试数据生成完成');
    return true;
  } catch (error) {
    colorLog('red', `✗ 测试数据生成失败: ${error.message}`);
    return false;
  }
}

async function startFrontend() {
  colorLog('cyan', '🌐 启动前端开发服务器...');
  
  try {
    const frontendPath = path.join(PROJECT_ROOT, 'frontend');
    
    // 检查是否已经在运行
    try {
      await execCommand('curl -s http://localhost:8083');
      colorLog('green', '✓ 前端服务已在运行');
      return true;
    } catch (error) {
      // 前端服务未运行，需要启动
    }
    
    colorLog('yellow', '正在启动前端开发服务器...');
    colorLog('yellow', '这将在新的终端窗口中运行，请不要关闭该窗口');
    
    // 在新的终端窗口中启动前端服务
    if (process.platform === 'win32') {
      spawn('cmd', ['/c', 'start', 'cmd', '/k', 'npm run serve'], {
        cwd: frontendPath,
        detached: true,
        stdio: 'ignore'
      });
    } else {
      spawn('gnome-terminal', ['--', 'bash', '-c', 'npm run serve; exec bash'], {
        cwd: frontendPath,
        detached: true,
        stdio: 'ignore'
      });
    }
    
    // 等待前端服务启动
    colorLog('yellow', '等待前端服务启动...');
    await delay(15000);
    
    // 验证前端服务
    let retries = 3;
    while (retries > 0) {
      try {
        await execCommand('curl -s http://localhost:8083');
        colorLog('green', '✓ 前端服务启动成功');
        return true;
      } catch (error) {
        retries--;
        if (retries > 0) {
          colorLog('yellow', `⏳ 前端服务未就绪，等待中... (剩余重试: ${retries})`);
          await delay(5000);
        }
      }
    }
    
    colorLog('yellow', '⚠ 前端服务可能需要更多时间启动，请稍后手动检查');
    return true;
  } catch (error) {
    colorLog('red', `✗ 前端服务启动失败: ${error.message}`);
    return false;
  }
}

function showSuccessMessage() {
  colorLog('green', '\n🎉 LowCodePlatform 启动成功！\n');
  
  console.log('📍 访问地址:');
  console.log('  • 前端界面 (开发模式): http://localhost:8083');
  console.log('  • 前端界面 (生产模式): http://localhost:8080');
  console.log('  • 设备服务 API: http://localhost:8081');
  console.log('  • 场景服务 API: http://localhost:8082');
  
  console.log('\n📊 测试数据:');
  console.log('  • 设备类型: 5 种 (智能灯泡、温度传感器、智能门锁、智能空调、安防摄像头)');
  console.log('  • 设备实例: 9 个');
  console.log('  • 场景配置: 4 个 (回家模式、睡眠模式、离家模式、安防模式)');
  
  console.log('\n📖 使用指南:');
  console.log('  • 查看详细测试文档: docs/TESTING_GUIDE.md');
  console.log('  • 重新生成测试数据: node scripts/generate-test-data.js');
  console.log('  • 停止所有服务: docker-compose down');
  
  console.log('\n🛠 常用命令:');
  console.log('  • 查看服务状态: docker-compose ps');
  console.log('  • 查看服务日志: docker-compose logs');
  console.log('  • 重启服务: docker-compose restart');
  
  colorLog('cyan', '\n开始您的测试之旅吧！ 🚀\n');
}

function showFailureMessage() {
  colorLog('red', '\n❌ 启动过程中遇到问题\n');
  
  console.log('🔧 故障排除建议:');
  console.log('  1. 检查 Docker 是否正常运行');
  console.log('  2. 确保所需端口未被占用');
  console.log('  3. 查看详细错误信息');
  console.log('  4. 参考 docs/TESTING_GUIDE.md 中的常见问题部分');
  
  console.log('\n📞 获取帮助:');
  console.log('  • 查看服务日志: docker-compose logs');
  console.log('  • 手动启动服务: docker-compose up -d');
  console.log('  • 查看测试文档: docs/TESTING_GUIDE.md');
  
  colorLog('yellow', '\n如需帮助，请查看项目文档或联系开发团队。\n');
}

async function main() {
  console.log('');
  colorLog('bright', '🚀 LowCodePlatform 快速启动脚本');
  colorLog('bright', '=====================================');
  console.log('');
  
  try {
    // 1. 检查环境依赖
    const prereqsOk = await checkPrerequisites();
    if (!prereqsOk) {
      showFailureMessage();
      process.exit(1);
    }
    
    // 2. 检查端口占用
    await checkPorts();
    
    // 3. 安装依赖
    const depsOk = await installDependencies();
    if (!depsOk) {
      showFailureMessage();
      process.exit(1);
    }
    
    // 4. 启动后端服务
    const servicesOk = await startServices();
    if (!servicesOk) {
      showFailureMessage();
      process.exit(1);
    }
    
    // 5. 检查服务健康状态
    const healthOk = await checkServiceHealth();
    if (!healthOk) {
      colorLog('yellow', '⚠ 部分服务可能未完全就绪，但将继续执行...');
    }
    
    // 6. 生成测试数据
    const dataOk = await generateTestData();
    if (!dataOk) {
      colorLog('yellow', '⚠ 测试数据生成失败，但服务已启动，您可以稍后手动生成');
    }
    
    // 7. 启动前端服务
    const frontendOk = await startFrontend();
    if (!frontendOk) {
      colorLog('yellow', '⚠ 前端服务启动可能有问题，请手动检查');
    }
    
    // 8. 显示成功信息
    showSuccessMessage();
    
  } catch (error) {
    colorLog('red', `\n❌ 启动过程中发生错误: ${error.message}\n`);
    showFailureMessage();
    process.exit(1);
  }
}

// 处理 Ctrl+C 信号
process.on('SIGINT', () => {
  colorLog('yellow', '\n\n⏹ 启动过程被用户中断');
  process.exit(0);
});

// 运行主函数
if (require.main === module) {
  main();
}

module.exports = {
  main,
  checkPrerequisites,
  startServices,
  generateTestData
};