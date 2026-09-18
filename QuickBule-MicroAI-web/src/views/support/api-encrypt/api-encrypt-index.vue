<!--
  * 接口加密、解密（现代化：说明横幅 + Demo 卡片，接口调用保持不变）
  *
-->
<template>
  <div class="encrypt-page">
    <!---------- 说明横幅 begin ---------->
    <a-card :bordered="false" class="banner-card">
      <div class="banner">
        <div class="banner-icon">
          <LockOutlined />
        </div>
        <div class="banner-text">
          <div class="banner-title">接口加解密 API Encrypt <a-tag color="orange" style="font-weight: 600;">三级必备</a-tag></div>
          <div class="banner-desc">
            对前端请求参数进行加密、后端返回结果进行解密，防止敏感数据在传输过程中被窃取或篡改。支持国密 SM4、AES 加密算法，通过注解与 axios 拦截器自动完成加解密，业务代码无感知。
          </div>
        </div>
        <div class="banner-algo">
          <span class="algo-label">当前加密算法</span>
          <span class="algo-tag">SM4</span>
        </div>
      </div>
      <div class="usage">
        <div class="usage-item">
          <span class="tag">前端</span>
          <code>lib/encrypt.js + lib/axios.js</code>
          <span class="desc">请求参数加密、响应解密由 axios 拦截器自动完成</span>
        </div>
        <div class="usage-item">
          <span class="tag">后端</span>
          <code>@ApiEncrypt / @ApiDecrypt</code>
          <span class="desc">注解位于 com.budaos.common.module.support.apiencrypt 包</span>
        </div>
        <div class="usage-item">
          <span class="tag">算法切换</span>
          <code>ApiEncryptServiceSmImpl / ApiEncryptServiceAesImpl</code>
          <span class="desc">切换后端实现类即可在 SM4 与 AES 之间切换</span>
        </div>
      </div>
    </a-card>
    <!---------- 说明横幅 end ---------->

    <!---------- 一、请求参数加密 Demo begin ---------->
    <a-card :bordered="false" class="demo-card">
      <template #title>
        <span class="card-title">
          <ExperimentOutlined class="card-title-icon" />
          一、请求加密 Demo
        </span>
      </template>
      <a-form class="qb-query-form">
        <a-row class="qb-query-form-row">
          <a-form-item label="姓名" class="qb-query-form-item">
            <a-input v-model:value="requestEncryptForm.name" placeholder="请输入姓名" allow-clear />
          </a-form-item>
          <a-form-item label="年龄" class="qb-query-form-item">
            <a-input-number v-model:value="requestEncryptForm.age" placeholder="请输入年龄" style="width: 160px" />
          </a-form-item>
          <a-form-item class="qb-query-form-item">
            <a-button type="primary" @click="testRequestEncrypt">
              <template #icon>
                <ExperimentOutlined />
              </template>
              测试：请求加密
            </a-button>
          </a-form-item>
        </a-row>
      </a-form>
      <div class="result-area">
        <div v-if="requestEncryptFormStr" class="result-item">
          <span class="result-label">请求参数</span>
          <code class="result-code">{{ requestEncryptFormStr }}</code>
        </div>
        <div v-if="requestEncryptFormEncryptStr" class="result-item">
          <span class="result-label">请求参数（加密后）</span>
          <code class="result-code result-cipher">{{ requestEncryptFormEncryptStr }}</code>
        </div>
        <div v-if="requestEncryptResponse" class="result-item">
          <span class="result-label">返回结果</span>
          <code class="result-code">{{ requestEncryptResponse }}</code>
        </div>
      </div>
    </a-card>
    <!---------- 一、请求参数加密 Demo end ---------->

    <!---------- 二、返回结果解密 Demo begin ---------->
    <a-card :bordered="false" class="demo-card">
      <template #title>
        <span class="card-title">
          <ExperimentOutlined class="card-title-icon" />
          二、返回加密 Demo
        </span>
      </template>
      <a-form class="qb-query-form">
        <a-row class="qb-query-form-row">
          <a-form-item label="姓名" class="qb-query-form-item">
            <a-input v-model:value="responseEncryptForm.name" placeholder="请输入姓名" allow-clear />
          </a-form-item>
          <a-form-item label="年龄" class="qb-query-form-item">
            <a-input-number v-model:value="responseEncryptForm.age" placeholder="请输入年龄" style="width: 160px" />
          </a-form-item>
          <a-form-item class="qb-query-form-item">
            <a-button type="primary" @click="testResponseEncrypt">
              <template #icon>
                <ExperimentOutlined />
              </template>
              测试：返回加密
            </a-button>
          </a-form-item>
        </a-row>
      </a-form>
      <div class="result-area">
        <div v-if="responseEncryptFormStr" class="result-item">
          <span class="result-label">请求参数</span>
          <code class="result-code">{{ responseEncryptFormStr }}</code>
        </div>
        <div v-if="responseEncryptStr" class="result-item">
          <span class="result-label">返回结果（加密后）</span>
          <code class="result-code result-cipher">{{ responseEncryptStr }}</code>
        </div>
        <div v-if="responseStr" class="result-item">
          <span class="result-label">返回结果（解密后）</span>
          <code class="result-code">{{ responseStr }}</code>
        </div>
      </div>
    </a-card>
    <!---------- 二、返回结果解密 Demo end ---------->

    <!---------- 三、请求和返回都加密 Demo begin ---------->
    <a-card :bordered="false" class="demo-card">
      <template #title>
        <span class="card-title">
          <ExperimentOutlined class="card-title-icon" />
          三、请求和返回都加密 Demo
        </span>
      </template>
      <a-form class="qb-query-form">
        <a-row class="qb-query-form-row">
          <a-form-item label="姓名" class="qb-query-form-item">
            <a-input v-model:value="form.name" placeholder="请输入姓名" allow-clear />
          </a-form-item>
          <a-form-item label="年龄" class="qb-query-form-item">
            <a-input-number v-model:value="form.age" placeholder="请输入年龄" style="width: 160px" />
          </a-form-item>
          <a-form-item class="qb-query-form-item">
            <a-button type="primary" @click="testBoth">
              <template #icon>
                <ExperimentOutlined />
              </template>
              测试：请求和返回都加密
            </a-button>
          </a-form-item>
        </a-row>
      </a-form>
      <div class="result-area">
        <div v-if="formStr" class="result-item">
          <span class="result-label">请求参数</span>
          <code class="result-code">{{ formStr }}</code>
        </div>
        <div v-if="formEncryptStr" class="result-item">
          <span class="result-label">请求参数（加密后）</span>
          <code class="result-code result-cipher">{{ formEncryptStr }}</code>
        </div>
        <div v-if="responseEncrypt" class="result-item">
          <span class="result-label">返回结果（加密后）</span>
          <code class="result-code result-cipher">{{ responseEncrypt }}</code>
        </div>
        <div v-if="responseDecryptStr" class="result-item">
          <span class="result-label">返回结果（解密后）</span>
          <code class="result-code">{{ responseDecryptStr }}</code>
        </div>
      </div>
    </a-card>
    <!---------- 三、请求和返回都加密 Demo end ---------->

    <!---------- 四、测试数组 Demo begin ---------->
    <a-card :bordered="false" class="demo-card">
      <template #title>
        <span class="card-title">
          <ExperimentOutlined class="card-title-icon" />
          四、测试数组 Demo
        </span>
      </template>
      <a-form class="qb-query-form">
        <a-row class="qb-query-form-row">
          <a-form-item class="qb-query-form-item">
            <a-button type="primary" @click="testArray">
              <template #icon>
                <ExperimentOutlined />
              </template>
              测试：数组加解密
            </a-button>
          </a-form-item>
        </a-row>
      </a-form>
      <div class="result-area">
        <div v-if="arrayFormStr" class="result-item">
          <span class="result-label">请求参数</span>
          <code class="result-code">{{ arrayFormStr }}</code>
        </div>
        <div v-if="arrayFormEncryptStr" class="result-item">
          <span class="result-label">请求参数（加密后）</span>
          <code class="result-code result-cipher">{{ arrayFormEncryptStr }}</code>
        </div>
        <div v-if="arrayFormResponseEncrypt" class="result-item">
          <span class="result-label">返回结果（加密后）</span>
          <code class="result-code result-cipher">{{ arrayFormResponseEncrypt }}</code>
        </div>
        <div v-if="arrayFormResponseDecryptStr" class="result-item">
          <span class="result-label">返回结果（解密后）</span>
          <code class="result-code">{{ arrayFormResponseDecryptStr }}</code>
        </div>
      </div>
    </a-card>
    <!---------- 四、测试数组 Demo end ---------->
  </div>
</template>
<script setup>
  import { reactive, ref } from 'vue';
  import { LockOutlined, ExperimentOutlined } from '@ant-design/icons-vue';
  import { encryptApi } from '/@/api/support/api-encrypt-api';
  import { encryptData } from '/@/lib/encrypt';

  // ---------------------------- 第一种：请求参数加密 ----------------------------

  //请求参数加密
  const requestEncryptForm = reactive({
    age: 100, // 年龄
    name: 'budaos', //姓名
  });

  // 参数字符串
  const requestEncryptFormStr = ref('');
  // 参数字符串 加密
  const requestEncryptFormEncryptStr = ref('');
  // 返回结果
  const requestEncryptResponse = ref('');

  async function testRequestEncrypt() {
    // 参数加密
    requestEncryptFormStr.value = JSON.stringify(requestEncryptForm);
    requestEncryptFormEncryptStr.value = encryptData(requestEncryptForm);

    // 发送请求
    const result = await encryptApi.testRequestEncrypt(requestEncryptForm);
    requestEncryptResponse.value = JSON.stringify(result.data);
  }

  // ---------------------------- 第二种：返回结果解密 ----------------------------

  const responseEncryptForm = reactive({
    age: 100, // 年龄
    name: 'budaos', //姓名
  });

  const responseEncryptFormStr = ref('');
  const responseEncryptStr = ref('');
  const responseStr = ref('');

  async function testResponseEncrypt() {
    responseEncryptFormStr.value = JSON.stringify(responseEncryptForm);
    const result = await encryptApi.testResponseEncrypt(responseEncryptForm);
    responseEncryptStr.value = result.encryptData;
    responseStr.value = JSON.stringify(result.data);
  }

  // ---------------------------- 第三种：请求加密、返回解密 ----------------------------

  const form = reactive({
    age: 100, // 年龄
    name: 'budaos', //姓名
  });

  const formStr = ref('');
  const formEncryptStr = ref('');
  const responseEncrypt = ref('');
  const responseDecryptStr = ref('');

  async function testBoth() {
    formStr.value = JSON.stringify(form);
    formEncryptStr.value = encryptData(form);
    const result = await encryptApi.testDecryptAndEncrypt(form);
    responseEncrypt.value = result.encryptData;
    responseDecryptStr.value = JSON.stringify(result.data);
  }

  // ---------------------------- 第四种：测试数组 ----------------------------

  const arrayForm = reactive([
    {
      age: 1, // 年龄
      name: '卓1', //姓名
    },
    {
      age: 2, // 年龄
      name: '卓2', //姓名
    },
    {
      age: 3, // 年龄
      name: '卓3', //姓名
    },
  ]);

  const arrayFormStr = ref('');
  const arrayFormEncryptStr = ref('');
  const arrayFormResponseEncrypt = ref('');
  const arrayFormResponseDecryptStr = ref('');

  async function testArray() {
    arrayFormStr.value = JSON.stringify(arrayForm);
    arrayFormEncryptStr.value = encryptData(arrayForm);
    const result = await encryptApi.testArray(arrayForm);
    arrayFormResponseEncrypt.value = result.encryptData;
    arrayFormResponseDecryptStr.value = JSON.stringify(result.data);
  }
</script>

<style lang="less" scoped>
  .encrypt-page {
    .banner-card {
      margin-bottom: 16px;

      .banner {
        display: flex;
        align-items: center;
        gap: 20px;
        flex-wrap: wrap;
        margin-bottom: 16px;

        .banner-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 64px;
          height: 64px;
          border-radius: 16px;
          font-size: 34px;
          color: #1677ff;
          background: linear-gradient(135deg, rgba(22, 119, 255, 0.14), rgba(22, 119, 255, 0.04));
          flex-shrink: 0;
        }

        .banner-text {
          flex: 1;
          min-width: 240px;

          .banner-title {
            font-size: 18px;
            font-weight: 600;
            color: rgba(0, 0, 0, 0.88);
          }

          .banner-desc {
            margin-top: 6px;
            font-size: 13px;
            color: rgba(0, 0, 0, 0.55);
            line-height: 1.6;
          }
        }

        .banner-algo {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 14px;
          border-radius: 8px;
          background: rgba(22, 119, 255, 0.06);
          border: 1px dashed rgba(22, 119, 255, 0.35);
          flex-shrink: 0;

          .algo-label {
            font-size: 12px;
            color: rgba(0, 0, 0, 0.45);
          }

          .algo-tag {
            font-size: 13px;
            font-weight: 600;
            color: #1677ff;
            font-family: Consolas, Monaco, monospace;
          }
        }
      }

      .usage {
        padding: 14px 16px;
        border-radius: 8px;
        background: rgba(22, 119, 255, 0.05);
        border: 1px solid rgba(22, 119, 255, 0.12);

        .usage-item {
          display: flex;
          align-items: center;
          gap: 10px;
          flex-wrap: wrap;
          margin-bottom: 6px;
          font-size: 13px;

          &:last-child {
            margin-bottom: 0;
          }

          .tag {
            flex-shrink: 0;
            padding: 1px 8px;
            border-radius: 4px;
            font-size: 12px;
            color: #1677ff;
            background: rgba(22, 119, 255, 0.1);
            border: 1px solid rgba(22, 119, 255, 0.25);
          }

          code {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            background: rgba(0, 0, 0, 0.05);
            border: 1px solid rgba(0, 0, 0, 0.08);
          }

          .desc {
            color: rgba(0, 0, 0, 0.45);
          }
        }
      }
    }

    .demo-card {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .card-title {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;

        .card-title-icon {
          color: #1677ff;
        }
      }

      .result-area {
        margin-top: 4px;
        padding: 14px 16px;
        border-radius: 8px;
        background: rgba(0, 0, 0, 0.02);
        border: 1px solid rgba(0, 0, 0, 0.06);

        .result-item {
          display: flex;
          align-items: flex-start;
          gap: 10px;
          margin-bottom: 8px;
          font-size: 13px;

          &:last-child {
            margin-bottom: 0;
          }

          .result-label {
            flex-shrink: 0;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            color: rgba(0, 0, 0, 0.65);
            background: rgba(0, 0, 0, 0.04);
            border: 1px solid rgba(0, 0, 0, 0.06);
            line-height: 20px;
          }

          .result-code {
            flex: 1;
            min-width: 0;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-family: Consolas, Monaco, monospace;
            word-break: break-all;
            line-height: 20px;
            color: rgba(0, 0, 0, 0.85);
            background: rgba(255, 255, 255, 0.7);
            border: 1px solid rgba(0, 0, 0, 0.06);

            &.result-cipher {
              color: #d48806;
              background: rgba(250, 173, 20, 0.08);
              border-color: rgba(250, 173, 20, 0.25);
            }
          }
        }
      }
    }
  }

  /* 暗色模式适配 */
  :global([data-theme='dark']) {
    .banner-title {
      color: rgba(255, 255, 255, 0.88) !important;
    }

    .banner-desc,
    .algo-label,
    .desc {
      color: rgba(255, 255, 255, 0.55) !important;
    }

    .banner-algo {
      background: rgba(22, 119, 255, 0.12) !important;
      border-color: rgba(22, 119, 255, 0.35) !important;
    }

    .usage {
      background: rgba(22, 119, 255, 0.08) !important;
      border-color: rgba(22, 119, 255, 0.2) !important;
    }

    .usage-item code {
      background: rgba(255, 255, 255, 0.08) !important;
      border-color: rgba(255, 255, 255, 0.12) !important;
    }

    .result-area {
      background: rgba(255, 255, 255, 0.03) !important;
      border-color: rgba(255, 255, 255, 0.08) !important;
    }

    .result-label {
      color: rgba(255, 255, 255, 0.65) !important;
      background: rgba(255, 255, 255, 0.08) !important;
      border-color: rgba(255, 255, 255, 0.1) !important;
    }

    .result-code {
      color: rgba(255, 255, 255, 0.85) !important;
      background: rgba(255, 255, 255, 0.06) !important;
      border-color: rgba(255, 255, 255, 0.1) !important;

      &.result-cipher {
        color: #ffc53d !important;
        background: rgba(250, 173, 20, 0.12) !important;
        border-color: rgba(250, 173, 20, 0.3) !important;
      }
    }
  }
</style>
