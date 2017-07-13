package com.siinger.akkademo.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.siinger.akkademo.server.Server;
import com.siinger.akkademo.server.ServerInfo;
import com.siinger.akkademo.utils.ActorCommand;
import com.siinger.akkademo.utils.GsonUtil;
import com.siinger.akkademo.utils.PropertiesUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * ClassName: Agent <br/>
 * Function: Agent 测试类. <br/>
 * date: 2016年8月5日 下午5:05:15 <br/>
 *
 * @author siinger
 * @version
 * @since JDK 1.7
 */
public class Agent {

	Logger logger = Logger.getLogger(this.getClass());

	public void init() {
		PropertiesUtils.load("core.properties");
	}

	public static void main(String[] args) {
		// 缓存管理器
		CacheManager manager = CacheManager.newInstance(Server.class.getResourceAsStream("/ehcache-rmi.xml"));// new
																												// CacheManager(fileName);
		System.out.println(manager.getActiveConfigurationText());
		// 取出所有的cacheName
		String names[] = manager.getCacheNames();
		for (String name : names) {
			System.out.println(name);// 输出所有Cache名称
		}
		Agent agent = new Agent();
		agent.init();
		// 创建client任务
		String clientAddress = "akka.tcp://ServerSystem@" + PropertiesUtils.get("client.ip") + ":"
				+ PropertiesUtils.get("client.port") + "/user/serverActor";

		// 本程序操作的缓存对象
		Cache clientCache = manager.getCache("clientCache");// 得到本程序操作的cache
		clientCache.put(new Element(PropertiesUtils.get("clientId"), clientAddress));
		Cache serverCache = manager.getCache("serverCache");
		List<?> list = serverCache.getKeys();
		// 创建remote 接受命令
		Config config = ConfigFactory.load("agent-application.conf");
		final ActorSystem actorSystem = ActorSystem.create("ServerSystem",config.getConfig("ServerSys"));
		actorSystem.actorOf(Props.create(AgentActor.class), "serverActor");
		for(int i=0;i<100;i++){
			for (Object obj : list) {
				Element element = serverCache.get(obj);
				ServerInfo info = GsonUtil.jsonToBean(element.getObjectValue().toString(), ServerInfo.class);
				final ActorSelection remoteActor = actorSystem.actorSelection(info.getAddress());
				final ActorRef actor = actorSystem.actorOf(Props.create(AgentActor.class));
				remoteActor.tell("msg:"+i, actor);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
