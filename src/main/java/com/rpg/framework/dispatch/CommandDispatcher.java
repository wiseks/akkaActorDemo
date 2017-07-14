package com.rpg.framework.dispatch;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.google.protobuf.MessageLite;
import com.rpg.framework.annotation.MessageHandler;
import com.rpg.framework.annotation.MessageMapping;


/**
 * 命令分发器
 */
public class CommandDispatcher {

	private final Log log = LogFactory.getLog(this.getClass());

	private final Map<Class<?>, CommandHandlerHolder> handlers = new LinkedHashMap<Class<?>, CommandHandlerHolder>();
	private final Map<String, Boolean> handlerCloses = new ConcurrentHashMap<String, Boolean>();
	
	public boolean closeMsg(String msgName){
		Boolean isClose = handlerCloses.get(msgName);
		if(isClose==null){
			return false;
		}
		isClose = true;
		handlerCloses.put(msgName, isClose);
		return true;
	}

	public void dispatch(MessageLite message) {
		if (message == null)
			return;

		long starttime = System.currentTimeMillis();

		CommandHandlerHolder holder = handlers.get(message.getClass());
		boolean isClose = false;// handlerCloses.get(cmd.getMessage().getClass().getSimpleName());
		if (null == holder || null == holder.method || null == holder.owner) {
			log.error("RECIVE|No handler or method for message:" + message);
			return;
		}
		if(isClose){
			log.error("RECIVE|Close handler or method for message:" + message);
			return;
		}
//		if (!serverConfig.isDebug()) { // 是否调试模式
//			if (holder.noCheck()) { // 有些协议不需要如何安全校验，比如登录，看战报
//
//			} else {
//				if (holder.checkLogin()) {// 是否需要登录后才能操作
//					Attachment attachment = (Attachment) context.getAttachment();
//					if (attachment == null || !sessionHolder.isOnline(attachment.getPlayerId())) {
//						context.getChannel().close();
//						log.warn("no login...");
//						return;
//					}
//				}
//			}
//		}
		
		try {
			Object returnValue = null;
			if (holder.getParamSize() == 1)
				returnValue = holder.method.invoke(holder.owner, message);
			if (holder.getParamSize() == 2)
				returnValue = holder.method.invoke(holder.owner, message);
			else if (holder.getParamSize() == 3)
				returnValue = holder.method.invoke(holder.owner, message);

			if (returnValue != null ){
				
			} else {
			}
		} catch (Exception e) {
			log.error("ERROR|Dispatcher method error occur. head:" + message.getClass().getName(), e);
		} finally {
		}

		long endtime = System.currentTimeMillis();
		long usetime = endtime - starttime;
		if (usetime > 500)
			log.info(">>>>>>>cmd:" + message.getClass().getName() + " use time:" + usetime);
	}

	@PostConstruct
	public void init(ApplicationContext applicationContext) {
		// 从sping上下文取出所有消息处理器
		Map<String, Object> handlerMap = applicationContext.getBeansWithAnnotation(MessageHandler.class);

		for (Object obj : handlerMap.values()) {
			Class<?> clazz = obj.getClass();

			final Class<?> userType = ClassUtils.getUserClass(obj.getClass());

			// 找到所有处理方法
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				// 判断处理方法是否符合要求
				MessageMapping cmd = method.getAnnotation(MessageMapping.class);
				Class<?>[] paramTypes = method.getParameterTypes();
				if (cmd == null || paramTypes.length == 0) {
					continue;
				}
				// 把处理方法加入缓存中
				Class<?> mappingClass = getMappingForMethod(method, userType);
				int paramSize = paramTypes.length;
				CommandHandlerHolder holder = new CommandHandlerHolder(obj, method, paramSize, cmd);
				handlers.put(mappingClass, holder);
				handlerCloses.put(mappingClass.getSimpleName(), false);
				log.info(("START|Init HandlerMethod: " + clazz.getSimpleName() + " --> " + method.getName()
						+ " --> cmd:" + cmd.toString()));
			}
		}
		log.info("START|Init HandlerMethod Count: " + handlers.size());
	}

	protected Class<?> getMappingForMethod(Method method, Class<?> clazz) {
		MessageMapping methodAnnotation = AnnotationUtils.findAnnotation(method, MessageMapping.class);
		if (methodAnnotation != null) {
			return methodAnnotation.value();
		}
		return null;
	}

	/**
	 * 消息处理方法容器
	 */
	public static class CommandHandlerHolder {

		public CommandHandlerHolder() {
		}

		public CommandHandlerHolder(Object owner, Method m, int paramSize, MessageMapping cmd) {
			this.owner = owner;
			this.method = m;
			this.command = cmd;
			this.paramSize = paramSize;
		}

		private Object owner;
		private Method method;
		private MessageMapping command;
		private int paramSize;

		public int getParamSize() {
			return paramSize;
		}

		/**
		 * 管理员协议
		 */
		public boolean isAdmin() {
			return command.admin();
		}

		/**
		 * 是否需要登錄
		 * 
		 * @return
		 */
		public boolean checkLogin() {
			return command.checkLogin();
		}

		/**
		 * 不需要任何安全验证
		 * 
		 * @return
		 */
		public boolean noCheck() {
			return command.nocheck();
		}
	}

	public static String BinToHex(byte[] buf) {
		final char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		if (buf.length < 1)
			return " ";
		StringBuffer result = new StringBuffer(buf.length * 2);
		for (int i = 0; i < buf.length; i++) {
			result.append(digit[(buf[i] >> 4 & 0x0F)]);
			result.append(digit[(buf[i] & 0x0F)]);
			if ((i + 1) % 4 == 0) {
				result.append(" ");
			}
		}

		return result.toString().toLowerCase();
	}

}
