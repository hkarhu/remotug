package fi.uef.remotug.net.server;

import fi.uef.remotug.net.client.RemoteClient;
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

public class RemotugServer {

	private final ChannelGroup allClients;
	private final EventLoopGroup acceptEventLoopGroup;
	private final EventLoopGroup workerEventLoopGroup;

	public RemotugServer(String addr, int port) throws RuntimeException {
		acceptEventLoopGroup = new NioEventLoopGroup();
		workerEventLoopGroup = new NioEventLoopGroup();
		allClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

		// --
		
		ServerBootstrap bootstrap = new ServerBootstrap();

		bootstrap.group(acceptEventLoopGroup, workerEventLoopGroup)
				.channel(NioServerSocketChannel.class)
//				.handler(new LoggingHandler(LogLevel.TRACE))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ObjectEncoder());
						ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
						ch.pipeline().addLast(new ServerHandler(allClients));
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
		RemotugServer s = new RemotugServer("127.0.0.1", 12345);
		
		RemoteClient c = new RemoteClient("127.0.0.1", 12345);
		
		System.out.println("read");
		System.in.read();
		System.out.println("shutdown");
		
		c.shutdown();
		s.shutdown();
	}
	
}
