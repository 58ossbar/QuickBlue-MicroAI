<template>
  <div class="architecture-container">
    <div class="architecture-title">QuickBlue 微服务架构</div>

    <div class="merged-architecture">
      <!-- 左侧：架构图 -->
      <div class="arch-diagram">
        <div class="arch-box gateway">
          <div class="arch-icon">🌐</div>
          <div class="arch-name">Gateway</div>
          <div class="arch-desc">API网关 (8080)</div>
        </div>

        <div class="arch-connector">↓</div>

        <div class="arch-row">
          <div class="arch-box system">
            <div class="arch-icon">🛡️</div>
            <div class="arch-name">System</div>
            <div class="arch-desc">系统服务</div>
          </div>

          <div class="arch-box business">
            <div class="arch-icon">💼</div>
            <div class="arch-name">Business</div>
            <div class="arch-desc">业务服务</div>
          </div>

          <div class="arch-box support">
            <div class="arch-icon">🔧</div>
            <div class="arch-name">Support</div>
            <div class="arch-desc">支撑服务</div>
          </div>

          <div class="arch-box ai">
            <div class="arch-icon">🤖</div>
            <div class="arch-name">AI</div>
            <div class="arch-desc">AI服务</div>
          </div>
        </div>

        <div class="arch-connector">↓</div>

        <div class="arch-box common">
          <div class="arch-icon">📦</div>
          <div class="arch-name">Common</div>
          <div class="arch-desc">公共模块</div>
        </div>

        <div class="arch-connector">↓</div>

        <div class="arch-row">
          <div class="arch-box db">
            <div class="arch-icon">🗄️</div>
            <div class="arch-name">PostgreSQL</div>
            <div class="arch-desc">关系型数据库</div>
          </div>

          <div class="arch-box redis">
            <div class="arch-icon">⚡</div>
            <div class="arch-name">Redis</div>
            <div class="arch-desc">缓存数据库</div>
          </div>

          <div class="arch-box nacos">
            <div class="arch-icon">🎯</div>
            <div class="arch-name">Nacos</div>
            <div class="arch-desc">配置中心</div>
          </div>
        </div>

        <div class="arch-connector">↓</div>

        <div class="arch-box admin-web">
          <div class="arch-icon">🎨</div>
          <div class="arch-name">Admin Web</div>
          <div class="arch-desc">管理前端 (3000)</div>
        </div>
      </div>

      <!-- 右侧：目录结构 -->
      <div class="arch-tree">
        <div class="tree-title">📁 项目目录结构</div>
        <div class="tree-content">
          <div class="tree-item root">
            <span class="tree-icon">📂</span>
            <span class="tree-name">QuickBlue-parent/</span>
          </div>

          <div class="tree-branch">
            <div class="tree-item">
              <span class="tree-indent">├─</span>
              <span class="tree-icon">📁</span>
              <span class="tree-name">QuickBlue-gateway/</span>
              <span class="tree-desc">API网关服务</span>
            </div>
            <div class="tree-item">
              <span class="tree-indent">├─</span>
              <span class="tree-icon">📁</span>
              <span class="tree-name">QuickBlue-admin/</span>
              <span class="tree-desc">管理员服务</span>
            </div>
            <div class="tree-item">
              <span class="tree-indent">├─</span>
              <span class="tree-icon">📁</span>
              <span class="tree-name">QuickBlue-api/</span>
              <span class="tree-desc">API接口定义</span>
            </div>
            <div class="tree-sub-branch">
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">api-system/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">api-business/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">api-support/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  └─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">api-ai/</span>
              </div>
            </div>
            <div class="tree-item">
              <span class="tree-indent">├─</span>
              <span class="tree-icon">📁</span>
              <span class="tree-name">QuickBlue-common/</span>
              <span class="tree-desc">公共模块</span>
            </div>
            <div class="tree-sub-branch">
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">common-core/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">common-database/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">common-redis/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  ├─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">common-security/</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">│  └─</span>
                <span class="tree-icon">📄</span>
                <span class="tree-name">common-web/</span>
              </div>
            </div>
            <div class="tree-item">
              <span class="tree-indent">└─</span>
              <span class="tree-icon">📁</span>
              <span class="tree-name">QuickBlue-modules/</span>
              <span class="tree-desc">业务模块</span>
            </div>
            <div class="tree-sub-branch">
              <div class="tree-item">
                <span class="tree-indent">   ├─</span>
                <span class="tree-icon">📁</span>
                <span class="tree-name">system/</span>
                <span class="tree-desc">系统服务</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">   ├─</span>
                <span class="tree-icon">📁</span>
                <span class="tree-name">business/</span>
                <span class="tree-desc">业务服务</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">   ├─</span>
                <span class="tree-icon">📁</span>
                <span class="tree-name">support/</span>
                <span class="tree-desc">支撑服务</span>
              </div>
              <div class="tree-item">
                <span class="tree-indent">   └─</span>
                <span class="tree-icon">📁</span>
                <span class="tree-name">ai/</span>
                <span class="tree-desc">AI服务</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 技术栈 -->
    <div class="tech-section">
      <h4>🔧 技术栈</h4>
      <div class="tech-list">
        <span class="tech-item" style="background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f;">Spring Boot 3.5.10</span>
        <span class="tech-item" style="background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff;">Spring Cloud Alibaba 2025</span>
        <span class="tech-item" style="background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591;">Nacos 2.4.3</span>
        <span class="tech-item" style="background: #f9f0ff; color: #722ed1; border: 1px solid #d3adf7;">Gateway</span>
        <span class="tech-item" style="background: #e6fffb; color: #13c2c2; border: 1px solid #87e8de;">OpenFeign</span>
        <span class="tech-item" style="background: #d9f7be; color: #389e0d; border: 1px solid #95de64;">PostgreSQL 18</span>
        <span class="tech-item" style="background: #ffccc7; color: #fa541c; border: 1px solid #ffa39e;">Redis</span>
        <span class="tech-item" style="background: #fff0f6; color: #eb2f96; border: 1px solid #ffadd2;">Sa-Token</span>
        <span class="tech-item" style="background: #e6fffb; color: #13c2c2; border: 1px solid #87e8de;">Vue 3</span>
        <span class="tech-item" style="background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e;">Ant Design Vue</span>
        <span class="tech-item" style="background: #fffbe6; color: #faad14; border: 1px solid #ffe58f;">Vite 5.2.12</span>
      </div>
    </div>
  </div>
</template>

<script setup>
// 微服务架构展示组件
</script>

<style scoped>
.architecture-container {
  background: #fafafa;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #f0f0f0;
}

.architecture-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 24px;
  text-align: center;
}

.merged-architecture {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

/* 左侧架构图 */
.arch-diagram {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.arch-row {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.arch-box {
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
  min-width: 100px;
  transition: all 0.3s;
}

.arch-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.arch-icon {
  font-size: 28px;
  margin-bottom: 6px;
}

.arch-name {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.arch-desc {
  font-size: 11px;
  color: #8c8c8c;
}

.arch-connector {
  font-size: 18px;
  color: #d9d9d9;
  margin: 4px 0;
}

/* 右侧目录树 */
.arch-tree {
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
}

.tree-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.tree-content {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.8;
}

.tree-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #262626;
}

.tree-item.root {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
}

.tree-indent {
  color: #d9d9d9;
  user-select: none;
}

.tree-icon {
  font-size: 14px;
}

.tree-name {
  color: #595959;
}

.tree-desc {
  color: #8c8c8c;
  font-size: 11px;
  margin-left: 8px;
  font-style: italic;
}

.tree-branch {
  margin-left: 8px;
}

.tree-sub-branch {
  margin-left: 24px;
}

/* 颜色样式 */
.gateway {
  border-color: #1890ff;
  background: #e6f7ff;
}

.system {
  border-color: #52c41a;
  background: #f6ffed;
}

.business {
  border-color: #faad14;
  background: #fffbe6;
}

.support {
  border-color: #13c2c2;
  background: #e6fffb;
}

.ai {
  border-color: #722ed1;
  background: #f9f0ff;
}

.common {
  border-color: #eb2f96;
  background: #fff0f6;
  width: 160px;
}

.db {
  border-color: #096dd9;
  background: #bae7ff;
}

.redis {
  border-color: #fa541c;
  background: #ffccc7;
}

.nacos {
  border-color: #52c41a;
  background: #b7eb8f;
}

.admin-web {
  border-color: #1890ff;
  background: #d6e4ff;
  width: 160px;
}

/* 技术栈 */
.tech-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.tech-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.tech-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-item {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
}

.tech-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式 */
@media (max-width: 1200px) {
  .merged-architecture {
    grid-template-columns: 1fr;
  }
}
</style>
