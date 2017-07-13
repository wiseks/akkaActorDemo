package com.siinger.akkademo.server;

import java.util.List;

import com.siinger.akkademo.client.AgentActor;
import com.siinger.akkademo.utils.GsonUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * ClassName: Master <br/>
 * Function: Master，连接client，并定时发送任务给client. <br/>
 * date: 2016年8月5日 下午5:06:58 <br/>
 *
 * @author siinger
 * @version
 * @since JDK 1.7
 */
public class Server {
	public static void main(String[] args) {
		System.out.println("start server……");
		// 缓存管理器
		CacheManager manager = CacheManager.newInstance(Server.class.getResourceAsStream("/ehcache-rmi.xml"));// new
																												// CacheManager(fileName);
		System.out.println(manager.getActiveConfigurationText());
		// 取出所有的cacheName
		String names[] = manager.getCacheNames();
		for (String name : names) {
			System.out.println("config cacheName:"+name);// 输出所有Cache名称
		}
		Cache serverCache = manager.getCache("serverCache");
		Config config = ConfigFactory.load("master-application.conf");
		Config childConfig = config.getConfig("ServerSys");
		Config addressConfig = childConfig.getConfig("akka").getConfig("remote").getConfig("netty.tcp");
		String hostname = addressConfig.getString("hostname");
		int port = addressConfig.getInt("port");
		String serverId = config.getString("serverId");
		String serverAddress = "akka.tcp://ServerSystem@" + hostname + ":"+ port + "/user/serverActor";
		String serverIp = "ServerSys.akka.remote.netty.tcp.hostname=" + hostname;
		ServerInfo info = new ServerInfo();
		info.setAddress(serverAddress);
		info.setIp(serverIp);
		info.setServerId(serverId);
		String json = GsonUtil.beanToJson(info);
		serverCache.put(new Element(info.getServerId(), json));
		
		
		
		ActorSystem actorSystem = ActorSystem.create("ServerSystem", childConfig);
		actorSystem.actorOf(Props.create(ServerActor.class), "serverActor");
		
		List<?> list = serverCache.getKeys();
		for(int i=0;i<100;i++){
			for (Object obj : list) {
				Element element = serverCache.get(obj);
				ServerInfo info1 = GsonUtil.jsonToBean(element.getObjectValue().toString(), ServerInfo.class);
				if(!info1.getServerId().equals(info.getServerId())){
					ActorSelection remoteActor = actorSystem.actorSelection(info1.getAddress());
					ActorRef actor = actorSystem.actorOf(Props.create(AgentActor.class));
					remoteActor.tell("serverId="+info.getServerId()+" send msg:"+i, actor);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
//		ExecutorService executor = Executors.newFixedThreadPool(3);
//		// 启动Remote
//		executor.execute(new Runnable() {
//
//			public void run() {
//				Config config = ConfigFactory.load("master-application.conf");
//				final ActorSystem actorSystem = ActorSystem.create("ServerSystem", config.getConfig("ServerSys"));
//				actorSystem.actorOf(Props.create(ServerActor.class), "serverActor");
//				// DeployAgentAndExec.actorSystem = actorSystem;
//				List<?> list = clientCache.getKeys();
//				for(Object obj : list){
//					Element clientAddress = clientCache.get(obj);
//					String str = clientAddress.getObjectValue().toString();
//					final ActorSelection remoteActor = actorSystem.actorSelection(str);
//					actorSystem.scheduler().schedule(Duration.create(10, SECONDS), Duration.create(10, SECONDS),
//							new Runnable() {
//								public void run() {
//									final ActorRef actor = actorSystem.actorOf(Props.create(ServerActor.class));
//									remoteActor.tell(ActorCommand.DO_SOMETHING, actor);
//								}
//							}, actorSystem.dispatcher());
//				}
//			}
//		});
	}
}
