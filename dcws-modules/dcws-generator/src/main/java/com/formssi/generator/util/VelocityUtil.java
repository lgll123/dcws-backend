package com.formssi.generator.util;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Velocity工具类,根据模板内容生成文件
 */
public class VelocityUtil {
	private VelocityUtil() {
		super();
	}

	static {
		// 禁止输出日志
		Velocity.setProperty("runtime.log.logsystem.class", new NullLogChute());

		Velocity.init();
	}

	private final static String LOG_TAG = "velocity_log";

	public static String generate(VelocityContext context, String template) {
		StringReader reader = new StringReader(template);
		StringWriter writer = new StringWriter();
		// 不用vm文件
		Velocity.evaluate(context, writer, LOG_TAG, reader);

		try {
			writer.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}
}
