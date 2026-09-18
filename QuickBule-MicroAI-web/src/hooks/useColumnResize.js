/*
 * 表格列宽拖拽调整
 *
 * 用法:
 *   import { useColumnResize } from '/@/hooks/useColumnResize';
 *   useColumnResize(tableWrapperRef, columns);
 */

import { onMounted, onUnmounted } from 'vue';

export function useColumnResize(tableWrapperRef, columnsRef, options = {}) {
  const { minWidth = 60, excludeSelectors = ['.ant-table-selection-column', '.ant-table-row-expand-icon-cell'] } = options;

  let resizeState = null;
  let observer = null;

  function shouldSkipColumn(th) {
    return excludeSelectors.some((sel) => th.matches(sel) || th.querySelector(sel));
  }

  function createResizeHandles(tableWrapper) {
    if (!tableWrapper) return;
    const headerCells = tableWrapper.querySelectorAll('.ant-table-thead th');
    headerCells.forEach((th) => {
      if (shouldSkipColumn(th)) return;
      if (th.querySelector('.column-resize-handle')) return;

      const handle = document.createElement('span');
      handle.className = 'column-resize-handle';
      handle.addEventListener('mousedown', (e) => onMouseDown(e, th));
      th.appendChild(handle);
    });
  }

  function onMouseDown(e, th) {
    e.preventDefault();
    e.stopPropagation();

    const startX = e.pageX;
    const startWidth = th.getBoundingClientRect().width;

    resizeState = { th, startX, startWidth };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
    document.body.classList.add('resizing-column');
  }

  function onMouseMove(e) {
    if (!resizeState) return;
    const diff = e.pageX - resizeState.startX;
    const newWidth = Math.max(minWidth, resizeState.startWidth + diff);
    resizeState.th.style.width = newWidth + 'px';
    resizeState.th.style.minWidth = newWidth + 'px';

    // 同步更新同列所有 cell 的宽度
    let wrapper = tableWrapperRef?.value;
    if (wrapper && wrapper.$el) {
      wrapper = wrapper.$el;
    }
    if (!wrapper) wrapper = resizeState.th.closest('.ant-table-wrapper');
    if (!wrapper || wrapper.nodeType !== 1) return;
    const idx = Array.from(resizeState.th.parentElement.children).indexOf(resizeState.th);
    const rows = wrapper.querySelectorAll('table tr');
    rows.forEach((row) => {
      const cell = row.children[idx];
      if (cell) {
        cell.style.width = newWidth + 'px';
        cell.style.minWidth = newWidth + 'px';
      }
    });
  }

  function onMouseUp() {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
    document.body.classList.remove('resizing-column');

    if (!resizeState) return;
    const newWidth = parseInt(resizeState.th.style.width) || resizeState.th.offsetWidth;
    resizeState = null;

    // 不自动写回 columnsRef，避免与 TableOperator 的列设置冲突
    // 拖拽仅做视觉调整
  }

  function setupObserver(tableWrapper) {
    if (!tableWrapper) return;
    if (observer) observer.disconnect();

    createResizeHandles(tableWrapper);

    observer = new MutationObserver(() => {
      // 延迟执行，等待 DOM 渲染完成
      setTimeout(() => createResizeHandles(tableWrapper), 100);
    });
    observer.observe(tableWrapper, { childList: true, subtree: true, attributes: false });
  }

  onMounted(() => {
    // 等待表格渲染后初始化
    setTimeout(() => {
      // 兼容 tableRef 可能是 vue 组件实例（取 $el）或 DOM 元素
      let wrapper = tableWrapperRef?.value;
      if (wrapper && wrapper.$el) {
        wrapper = wrapper.$el;
      }
      if (wrapper && wrapper.nodeType === 1) {
        // wrapper 应是 .ant-table 元素，再找其所属的 .ant-table-wrapper
        const realWrapper = wrapper.closest?.('.ant-table-wrapper') || wrapper;
        setupObserver(realWrapper);
      } else {
        // fallback: 通过选择器查找（取第一个）
        const el = document.querySelector('.ant-table-wrapper');
        if (el) setupObserver(el);
      }
    }, 400);
  });

  onUnmounted(() => {
    if (observer) observer.disconnect();
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
    document.body.classList.remove('resizing-column');
  });
}
