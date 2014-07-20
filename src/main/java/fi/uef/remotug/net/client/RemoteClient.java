package fi.uef.remotug.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class RemoteClient {

	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	public RemoteClient(String address, int port) throws RuntimeException {
	    Bootstrap b = new Bootstrap()
		    .group(workerGroup)
	        .channel(NioSocketChannel.class)
	        .option(ChannelOption.SO_KEEPALIVE, true)
	        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
	        .handler(new ChannelInitializer<SocketChannel>() {
	            @Override
	            public void initChannel(SocketChannel ch) throws Exception {
	            	ch.pipeline()
//	            		.addLast(new LoggingHandler(LogLevel.TRACE))
	                	.addLast(new ObjectEncoder())
		            	.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
		                .addLast(new ClientHandler())
		                ;
	            }
        });

		RuntimeException ex = null;
        try {
        	ChannelFuture f = b.connect(address, port).sync();
        	
        	if (!f.isSuccess()) {
            	throw new RuntimeException(f.cause());
            }
        	
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
			
		} finally {
			if (ex != null) {
				shutdown();
				throw ex;
			}
		}
	}

	public void shutdown() {
		workerGroup.shutdownGracefully();
	}

}
