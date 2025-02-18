package com.formssi.common.minio.dto;

import lombok.Data;

@Data
public class FileInfo {
	/**
	 * 文件名
	 */
	private String fileName;

	/**
	 * 是否是文件夾
	 */
	private Boolean directory;
}