package com.budaos.common.excel.handler;

import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动列宽样式策略
 * <p>
 * 根据单元格内容自动调整列宽，提高 Excel 可读性
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
public class AutoColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

    /**
     * 最大列宽限制
     */
    private static final int MAX_COLUMN_WIDTH = 255;

    /**
     * 缓存：Sheet序号 -> (列索引 -> 列宽)
     */
    private final Map<Integer, Map<Integer, Integer>> cache = new HashMap<>(8);

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList,
                                  Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        boolean needSetWidth = isHead || !cellDataList.isEmpty();
        if (needSetWidth) {
            Map<Integer, Integer> maxColumnWidthMap = cache.computeIfAbsent(
                writeSheetHolder.getSheetNo(),
                k -> new HashMap<>(16)
            );

            Integer columnWidth = dataLength(cellDataList, cell, isHead);
            if (columnWidth >= 0) {
                // 限制最大列宽
                if (columnWidth > MAX_COLUMN_WIDTH) {
                    columnWidth = MAX_COLUMN_WIDTH;
                }

                Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
                if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
                    maxColumnWidthMap.put(cell.getColumnIndex(), columnWidth);
                    // 设置列宽（单位：字符宽度的1/256）
                    writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), columnWidth * 256);
                }
            }
        }
    }

    /**
     * 计算数据长度
     *
     * @param cellDataList 单元格数据列表
     * @param cell         单元格对象
     * @param isHead       是否是表头
     * @return 数据长度
     */
    private Integer dataLength(List<WriteCellData<?>> cellDataList, Cell cell, Boolean isHead) {
        if (isHead) {
            return cell.getStringCellValue().getBytes().length;
        }

        WriteCellData<?> cellData = cellDataList.get(0);
        CellDataTypeEnum type = cellData.getType();
        if (type == null) {
            return -1;
        }

        switch (type) {
            case STRING:
                // 处理换行符（数据需要提前解析好）
                int index = cellData.getStringValue().indexOf("\n");
                if (index != -1) {
                    return Math.max(
                        cellData.getStringValue().substring(0, index).getBytes().length,
                        cellData.getStringValue().substring(index + 1).getBytes().length
                    );
                }
                return cellData.getStringValue().getBytes().length;
            case BOOLEAN:
                return cellData.getBooleanValue().toString().getBytes().length;
            case NUMBER:
                return cellData.getNumberValue().toString().getBytes().length;
            case DATE:
                return cellData.getStringValue().getBytes().length;
            case EMPTY:
                return -1;
            default:
                return -1;
        }
    }
}
