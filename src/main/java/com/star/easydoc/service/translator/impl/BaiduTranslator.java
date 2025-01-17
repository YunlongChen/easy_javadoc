package com.star.easydoc.service.translator.impl;

import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import com.intellij.openapi.diagnostic.Logger;
import com.star.easydoc.common.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 百度翻译
 *
 * @author wangchao
 * @date 2019/09/01
 */
public class BaiduTranslator extends AbstractTranslator {
    private static final Logger LOGGER = Logger.getInstance(BaiduTranslator.class);

    private static final String URL = "http://api.fanyi.baidu.com/api/trans/vip/translate?from=auto&to=auto&appid=%s&salt=%s&sign=%s&q=%s";

    @Override
    public String translateEn2Ch(String text) {
        try {
            return get(text);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("请求百度翻译接口异常：请检查本地网络是否可连接外网，也有可能被百度限流", e);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String translateCh2En(String text) {
        try {
            return get(text);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("请求百度翻译接口异常：请检查本地网络是否可连接外网，也有可能被百度限流", e);
        }
        return StringUtils.EMPTY;
    }

    private String get(String text) throws InterruptedException {
        String result = "";
        for (int i = 0; i < 10; i++) {
            String salt = RandomStringUtils.randomNumeric(16);
            String sign = DigestUtils.md5Hex(getConfig().getAppId() + text + salt + getConfig().getToken());
            String eText = HttpUtil.encode(text);
            BaiduResponse response = JSON.parseObject(HttpUtil.get(String.format(URL, getConfig().getAppId(), salt, sign, eText)),
                BaiduResponse.class);
            if (response == null || "54003".equals(response.getErrorCode())) {
                Thread.sleep(500);
            } else {
                result = Objects.requireNonNull(response).getTransResult().get(0).getDst();
                break;
            }
        }
        return result;
    }

    private static class BaiduResponse {
        @JSONField(name = "error_code")
        private String errorCode;
        @JSONField(name = "error_msg")
        private String errorMsg;
        private String from;
        private String to;
        @JSONField(name = "trans_result")
        private List<TransResult> transResult;

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFrom() {
            return from;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getTo() {
            return to;
        }

        public void setTransResult(List<TransResult> transResult) {
            this.transResult = transResult;
        }

        public List<TransResult> getTransResult() {
            return transResult;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

    private static class TransResult {

        private String src;
        private String dst;

        public void setSrc(String src) {
            this.src = src;
        }

        public String getSrc() {
            return src;
        }

        public void setDst(String dst) {
            this.dst = dst;
        }

        public String getDst() {
            return dst;
        }

    }
}
