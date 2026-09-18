<!--
  * 通用表格导出组件（Excel / PDF）
  * 放在表格右上角，与 TableOperator 并列使用
  *
  * Props:
  *   columns    - 表格列定义 [{ title, dataIndex, key, ... }]
  *   dataSource - 表格数据 []
  *   fileName   - 导出文件名（不含扩展名）
  *   title      - PDF 文档标题（Excel 不使用）
-->
<template>
  <span>
    <a-tooltip title="导出">
      <a-dropdown :trigger="['click']">
        <a-button type="text" size="small">
          <template #icon><export-outlined /></template>
        </a-button>
        <template #overlay>
          <a-menu @click="handleExportClick">
            <a-menu-item key="excel">
              <file-excel-outlined />
              <span style="margin-left: 6px">导出 Excel</span>
            </a-menu-item>
            <a-menu-item key="pdf">
              <file-pdf-outlined />
              <span style="margin-left: 6px">导出 PDF</span>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </a-tooltip>
  </span>
</template>

<script setup>
  import { getCurrentInstance } from 'vue';
  import { message } from 'ant-design-vue';
  import { ExportOutlined, FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons-vue';
  import { sentry } from '/@/lib/sentry';
  import { exportApi } from '/@/api/support/export-api';
  import html2pdf from 'html2pdf.js';

  const props = defineProps({
    columns: {
      type: Array,
      default: () => [],
    },
    dataSource: {
      type: Array,
      default: () => [],
    },
    fileName: {
      type: String,
      default: '导出数据',
    },
    title: {
      type: String,
      default: '',
    },
  });

  // 获取 $enumPlugin
  const instance = getCurrentInstance();
  const enumPlugin = instance?.appContext?.config?.globalProperties?.$enumPlugin || null;

  // 常见的 boolean 字段对应的枚举名（启发式映射）
  // 优先级低于列上显式声明的 enumName
  const BOOLEAN_ENUM_MAP = {
    administratorFlag: 'FLAG_NUMBER_ENUM',
    disabledFlag: 'FLAG_NUMBER_ENUM',
    isAdmin: 'FLAG_NUMBER_ENUM',
    enabled: 'FLAG_NUMBER_ENUM',
    visibleFlag: 'FLAG_NUMBER_ENUM',
  };

  // ---------- 工具 ----------
  function getExportColumns() {
    return props.columns.filter((col) => {
      if (col.dataIndex === 'operate' || col.key === 'operate') return false;
      return !!(col.dataIndex || col.key);
    });
  }

  function getEnumNameForColumn(col) {
    // 1) 显式声明：col.enumName 或 col['enum-name']
    if (col.enumName) return col.enumName;
    if (col['enum-name']) return col['enum-name'];
    // 2) 启发式
    const key = col.dataIndex || col.key;
    if (key && BOOLEAN_ENUM_MAP[key]) return BOOLEAN_ENUM_MAP[key];
    return null;
  }

  function formatCellValue(rawValue, col) {
    if (rawValue === null || rawValue === undefined) return '';
    const enumName = getEnumNameForColumn(col);
    if (enumName && enumPlugin) {
      // 翻译失败（返回 ''）时回退原值
      const desc = enumPlugin.getDescByValue(enumName, rawValue);
      if (desc) return desc;
    }
    return rawValue;
  }

  function buildExportData() {
    const exportCols = getExportColumns();
    const headers = exportCols.map((col) => col.title);
    // 防御：dataSource 可能为 undefined / ref
    const source = Array.isArray(props.dataSource) ? props.dataSource : [];
    const rows = source.map((record) =>
      exportCols.map((col) => {
        const key = col.dataIndex || col.key;
        if (!key) return '';
        const raw = key.includes('.')
          ? key.split('.').reduce((obj, k) => (obj || {})[k], record) ?? ''
          : record[key] ?? '';
        return String(formatCellValue(raw, col) ?? '');
      })
    );
    return { headers, rows };
  }

  // ---------- Excel 导出（走后端 Apache POI，完整样式） ----------
  async function exportExcel() {
    try {
      const { headers, rows } = buildExportData();
      if (rows.length === 0) {
        message.warning('没有数据可导出');
        return;
      }

      const res = await exportApi.exportExcel({
        headers,
        rows,
        fileName: props.fileName,
        sheetName: props.title || props.fileName,
      });

      // 下载 blob
      const blob = res.data instanceof Blob ? res.data : new Blob([res.data]);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${props.fileName}.xlsx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);

      message.success('Excel 导出成功');
    } catch (error) {
      message.error('Excel 导出失败');
      sentry.captureError(error);
    }
  }

  // ---------- PDF 导出（新窗口渲染 → html2pdf） ----------
  async function exportPDF() {
    let pdfWindow = null;
    try {
      const { headers, rows } = buildExportData();
      if (rows.length === 0) {
        message.warning('没有数据可导出');
        return;
      }

      const html = buildPDFFullHtml(headers, rows);

      // 打开新窗口写入 HTML（确保完整渲染）
      pdfWindow = window.open('', '_blank');
      if (!pdfWindow) {
        message.error('浏览器阻止了弹窗，请允许后重试');
        return;
      }

      pdfWindow.document.write(html);
      pdfWindow.document.close();

      // 等新窗口完成渲染
      await new Promise((r) => setTimeout(r, 600));

      const opt = {
        margin: [10, 8, 10, 8],
        filename: `${props.fileName}.pdf`,
        image: { type: 'jpeg', quality: 0.95 },
        html2canvas: {
          scale: 2,
          useCORS: true,
          logging: false,
          backgroundColor: '#ffffff',
        },
        jsPDF: {
          unit: 'mm',
          format: 'a4',
          orientation: 'landscape',
        },
        pagebreak: { mode: ['avoid-all', 'css', 'legacy'] },
      };

      await html2pdf().set(opt).from(pdfWindow.document.body).save();
      pdfWindow.close();
      message.success('PDF 导出成功');
    } catch (error) {
      if (pdfWindow && !pdfWindow.closed) pdfWindow.close();
      message.error('PDF 导出失败');
      sentry.captureError(error);
    }
  }

  // ---------- 生成完整 PDF HTML ----------
  function buildPDFFullHtml(headers, rows) {
    const cellStyle = 'border:1px solid #333;padding:6px 10px;font-size:12px;text-align:center;';
    const thStyle = cellStyle + 'font-weight:bold;background:#4472C4;color:#fff;';

    const now = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    const exportTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;

    const titleHtml = props.title
      ? `<h2 style="text-align:center;margin:0 0 6px 0;font-size:16px;color:#333;">${props.title}</h2>`
      : '';
    const metaHtml = `<div style="text-align:center;margin:0 0 14px 0;font-size:11px;color:#666;">共 ${rows.length} 条数据 · 导出时间：${exportTime}</div>`;

    const headerCells = headers.map((h) => `<th style="${thStyle}">${h}</th>`).join('');
    const bodyRows = rows
      .map((row, idx) => {
        const bg = idx % 2 === 0 ? '#fff' : '#f5f7fa';
        return `<tr>${row.map((c) => `<td style="${cellStyle}background:${bg};">${c ?? ''}</td>`).join('')}</tr>`;
      })
      .join('');

    return `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>${props.fileName}</title></head>
<body style="font-family:'Microsoft YaHei',sans-serif;padding:16px;background:#fff;margin:0;">
${titleHtml}
${metaHtml}
<div style="display:flex;justify-content:center;">
<table style="border-collapse:collapse;border:1px solid #333;margin:0 auto;">
<thead><tr>${headerCells}</tr></thead>
<tbody>${bodyRows}</tbody>
</table>
</div>
</body></html>`;
  }

  // ---------- 下拉菜单点击 ----------
  function handleExportClick({ key }) {
    if (key === 'excel') {
      exportExcel();
    } else if (key === 'pdf') {
      exportPDF();
    }
  }
</script>
