package fi.uef.remotug.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import fi.conf.ae.routines.S;
import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.sensor.SensorListener;

public class Connection implements SensorListener {

	private List<ConnectionListener> serverListeners = new ArrayList<>();

	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Channel myChannel;
	
	public Connection(String address, int port) throws RuntimeException {

		S.debug("Establishing connection to " + address + ":" + port);
		
		Bootstrap b = new Bootstrap()
			.group(workerGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline()
	            		//.addLast(new LoggingHandler(LogLevel.TRACE))
						.addLast(new ObjectEncoder())
						.addLast(new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(getClass().getClassLoader())))
						.addLast(new ClientsideHandler(serverListeners));
				}
			});

		ChannelFuture f;
		f = b.connect(address, port);
		
		f.awaitUninterruptibly(10000);

		if (!f.isSuccess()) {
			JOptionPane.showMessageDialog(null, f.cause().getMessage(), "Connection error", JOptionPane.ERROR_MESSAGE);
			myChannel = null;
			return;
		} else {
			myChannel = f.channel();
		}
		
		S.debug("Connection succesfully created.");
		
	}
	
	public boolean isConnected(){
		return myChannel != null;
	}

	public void addListener(ConnectionListener listener) {
		this.serverListeners.add(listener);
	}

	public void writePacket(BasePacket packet){
		if(myChannel != null) myChannel.writeAndFlush(packet);
	}

	public void close(){
		myChannel = null;
		workerGroup.shutdownGracefully();
	}

	@Override
	public void newSensorDataArrived(float kg) {
		if(myChannel != null) myChannel.writeAndFlush(new DataPacket(kg));
	}
}

