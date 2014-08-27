package fi.uef.remotug.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RemotugServer {

	private final ChannelGroup allClients;
	private final EventLoopGroup acceptEventLoopGroup;
	private final EventLoopGroup workerEventLoopGroup;
	private final ServersideHandler serversideHandler;
	public RemotugServer(String addr, int port) throws RuntimeException {
		acceptEventLoopGroup = new NioEventLoopGroup();
		workerEventLoopGroup = new NioEventLoopGroup();
		allClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

		// --
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		serversideHandler = new ServersideHandler(allClients);
		bootstrap.group(acceptEventLoopGroup, workerEventLoopGroup)
				.channel(NioServerSocketChannel.class)
//				.handler(new LoggingHandler(LogLevel.TRACE))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()
	            			.addLast(new LoggingHandler(LogLevel.DEBUG))
	            			.addLast(new ObjectEncoder())
							.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
							.addLast(serversideHandler)
							;
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture f;
		RuntimeException ex = null;
		try {
			f = bootstrap.bind(addr, port).sync();
			
			if (!f.isSuccess()) {
				ex = new RuntimeException(f.cause());
			}
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				  public void run() {
					  serversideHandler.calculateAndSendBalances();
					  
					  long matchStarted = 0;
					  if((matchStarted = serversideHandler.matchStarted()) != ServersideHandler.NO_ACTIVE_MATCH) {
						  if(System.currentTimeMillis() - matchStarted >= ServersideHandler.MATCH_LENGTH) {
							  serversideHandler.endActiveMatch();
						  }
					  }
				  }
				}, 1000, 100);
			
		} catch (InterruptedException e) {
			ex = new RuntimeException(e);
			
		} finally {
			if (ex != null) {
				shutdown();
				throw ex;
			}
		}
	}

	public void shutdown() {
		acceptEventLoopGroup.shutdownGracefully();
		workerEventLoopGroup.shutdownGracefully();
	}
	
	public static void main(String[] args) throws IOException {
		RemotugServer s = new RemotugServer("0.0.0.0", 4575);
		
		//RemoteClient c = new RemoteClient("127.0.0.1", 12345);
		
		System.out.println("read");
		System.in.read();
		System.out.println("shutdown");
		
		//c.shutdown();
		s.shutdown();
	}
	
}
