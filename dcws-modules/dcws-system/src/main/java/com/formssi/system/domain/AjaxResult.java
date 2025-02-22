package com.formssi.system.domain;

import java.io.Serializable;

/**
 * 操作消息提醒
 */
public class AjaxResult implements Serializable {
    private static final long serialVersionUID = 1L;

    // 状态码
    private int code;

    // 返回消息
    private String msg;

    // 返回数据
    private Object data;

    /**
     * 初始化一个新创建的 AjaxResult 对象
     */
    public AjaxResult() {
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回消息
     */
    public AjaxResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回消息
     * @param data 返回数据
     */
    public AjaxResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success() {
        return success("操作成功");
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回消息
     * @return 成功消息
     */
    public static AjaxResult success(String msg) {
        return success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param data 返回数据
     * @return 成功消息
     */
    public static AjaxResult success(Object data) {
        return success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回消息
     * @param data 返回数据
     * @return 成功消息
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(200, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static AjaxResult error() {
        return error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回消息
     * @return 错误消息
     */
    public static AjaxResult error(String msg) {
        return error(500, msg);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回消息
     * @return 错误消息
     */
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg);
    }
}
