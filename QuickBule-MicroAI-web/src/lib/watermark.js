/*
 * 水印
 *

 */

import dayjs from 'dayjs';

/**
 *  水印DOM id
 */
const WATER_MARK_DOM_ID = 'budaos_water_mark';
let budaosWaterMarkIntervalId = null;

/**
 *
 * 因为modal的z-index为1000，所以为了modal的黑色背景隐藏掉，z-index为 999
 *
 * @param id
 * @param str - 可以是字符串或配置对象
 * @returns
 */

function setWatermark(id, str) {
    //删掉之前的水印
    if (document.getElementById(WATER_MARK_DOM_ID) !== null) {
        document.getElementById(WATER_MARK_DOM_ID).remove();
    }

    // 支持自定义水印配置
    let watermarkText = '';
    let watermarkConfig = {
        text: str,
        showDate: true,
        dateFormat: 'YYYY-MM-DD HH:mm',
        fontSize: 16,
        fontColor: 'rgba(190, 190, 190, 0.30)',
        rotate: -15
    };

    // 如果传入的是配置对象
    if (typeof str === 'object' && str !== null) {
        watermarkConfig = { ...watermarkConfig, ...str };
        watermarkText = watermarkConfig.text || '';
    } else {
        // 兼容旧的字符串传参方式
        watermarkText = str;
    }

    // 添加日期时间
    if (watermarkConfig.showDate) {
        watermarkText = watermarkText + ' ' + dayjs().format(watermarkConfig.dateFormat);
    }

    str = watermarkText;

    //创建一个画布
    const can = document.createElement('canvas');
    //设置画布的长宽
    can.width = 400;
    can.height = 200;

    const cans = can.getContext('2d');
    //旋转角度
    cans.rotate((watermarkConfig.rotate * Math.PI) / 180);
    cans.font = `${watermarkConfig.fontSize}px Microsoft JhengHei`;
    //设置填充绘画的颜色、渐变或者模式
    cans.fillStyle = watermarkConfig.fontColor;
    //设置文本内容的当前对齐方式
    cans.textAlign = 'left';
    //设置在绘制文本时使用的当前文本基线
    cans.textBaseline = 'middle';
    //在画布上绘制填色的文本（输出的文本，开始绘制文本的X坐标位置，开始绘制文本的Y坐标位置）
    cans.fillText(str, can.width / 8, can.height / 2);
    const div = document.createElement('div');
    div.id = WATER_MARK_DOM_ID;
    div.style.pointerEvents = 'none';
    div.style.top = '0px';
    div.style.left = '0px';
    div.style.position = 'absolute';
    div.style.zIndex = '99';
    div.style.width = '100%';
    div.style.height = '100%';
    div.style.background = 'url(' + can.toDataURL('image/png') + ') left top repeat';
    document.getElementById(id).appendChild(div);
}

const watermark = {
    show: function () {
        document.getElementById(WATER_MARK_DOM_ID).style.display = 'block';
    },
    hide: function () {
        document.getElementById(WATER_MARK_DOM_ID).style.display = 'hide';
    },
    // 该方法只允许调用一次
    set: function (id, str) {
        // 如果存在水印，则不允许再调用了
        if (document.getElementById(WATER_MARK_DOM_ID) !== null) {
            return;
        }

        setWatermark(id, str);

        //每隔1分钟检查一次水印
        budaosWaterMarkIntervalId = setInterval(() => {
            setWatermark(id, str);
        }, 60000);

        window.onresize = () => {
            setWatermark(id, str);
        };
    },
    // 清空水印
    clear: function () {
        let watermarkDom = document.getElementById(WATER_MARK_DOM_ID);
        if (watermarkDom) {
            watermarkDom.remove();
        }

        window.removeEventListener('resize', setWatermark);
        if (budaosWaterMarkIntervalId) {
            clearInterval(budaosWaterMarkIntervalId);
            budaosWaterMarkIntervalId = null;
        }
    },
};
export default watermark;
