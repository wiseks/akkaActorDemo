package com.rpg.framework.protobuf;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;

@Component
public class ProtobufMapping {
	
	public static final int HEAD_SZIE = 9;

	private static final String DEFATULT_PROTOBUF_PACKAGE = "com/message";

	private static final char CMD_SEPARATOR = '_';

	private static final String resourcePattern = "**/*Msg" + CMD_SEPARATOR + "*.class";

	private static final String DEFAULT_INSTANCE_METHOD = "getDefaultInstance";

	private static final Map<Short, MessageLite> cmd2Message = new HashMap<Short, MessageLite>();

	private static final Map<Class<?>, Short> messageClass2Cmd = new HashMap<Class<?>, Short>();

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

	@Value("com/message")
	private String basePackage = DEFATULT_PROTOBUF_PACKAGE;

	public ProtobufMapping() {
	}

	public ProtobufMapping(String basePackage) {
		this.basePackage = basePackage;
	}

	@PostConstruct
	public void initialize() {
		basePackage = ClassUtils.convertClassNameToResourcePath(basePackage);
		initProtobufClasses();
	}

	protected void initProtobufClasses() {
		if (!cmd2Message.isEmpty()) {
			return;
		}
		String[] basePackages = basePackage.split(",");
		for (String basePackageName : basePackages) {
			if(!StringUtils.isNotEmpty(basePackageName)){
				continue;
			}
//			if (Strings.isNullOrEmpty(basePackageName)) {
//				continue;
//			}
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackageName + "/"
					+ resourcePattern;
			try {
				Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
				for (Resource resource : resources) {
					if (!resource.isReadable()) {
						return;
					}
					MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
					ClassMetadata classMetadata = metadataReader.getClassMetadata();
					if (!GeneratedMessageLite.class.getName().equals(classMetadata.getSuperClassName())) {
						continue;
					}
					String className = classMetadata.getClassName();
					Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
					Method getDefaultInstance = ReflectionUtils.findMethod(clazz, DEFAULT_INSTANCE_METHOD);
					if (getDefaultInstance != null) {
						Short shortCmd = classCmd(className);
						MessageLite existMessage = cmd2Message.get(shortCmd);
						if (existMessage != null) {
							throw new IllegalStateException(String.format("Ambiguous message found. "
									+ "Cannot map message: %s onto: %s, There is already message: %s mapped", clazz,
									shortCmd, existMessage.getClass()));
						}
						MessageLite messageLite = (MessageLite) ReflectionUtils.invokeMethod(getDefaultInstance, null);
						cmd2Message.put(shortCmd, messageLite);
						messageClass2Cmd.put(clazz, shortCmd);
					}
				}
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			} catch (LinkageError e) {
			}
		}
	}

	public MessageLite message(short cmd) {
		return cmd2Message.get(cmd);
	}

	public MessageLite message(short cmd, byte[] byteData) throws Exception {
		MessageLite message = cmd2Message.get(cmd);
		if (message != null) {
			return message.getParserForType().parseFrom(byteData);
		} else {
		}
		return message;
	}

	public Short messageCmd(Class<?> clazz) {
		return messageClass2Cmd.get(clazz);
	}

	public static Short classCmd(String className) {
		String cmd = className.substring(className.indexOf(CMD_SEPARATOR) + 1);
		return Short.valueOf(cmd);
	}
}
