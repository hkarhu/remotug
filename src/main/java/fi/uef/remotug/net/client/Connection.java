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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import fi.conf.ae.routines.S;
import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.sensor.SensorListener;

public class Connection implements SensorListener {

	private List<ConnectionListener> serverListeners = new ArrayList<>();

	private Timer simulationTimer;

	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Channel myChannel;

	public Connection(String address, int port) throws RuntimeException {

		Bootstrap b = new Bootstrap()
		.group(workerGroup)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline()
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

			System.out.println("[client] connected.");
			
		} catch (InterruptedException e) {
			throw new RuntimeException(e);

		} finally {
			if (ex != null) {
				close();
				throw ex;
			}
		}

	}

	public void addListener(ConnectionListener listener) {
		this.serverListeners.add(listener);
	}

	public void writePacket(BasePacket packet){
		myChannel.writeAndFlush(packet);
	}

	public void close(){
		workerGroup.shutdownGracefully();
	}

	@Override
	public void newSensorDataArrived(float balance) {
		S.debug("Balance: " + balance);
		for(ConnectionListener l : serverListeners){
			l.gameBalanceChanged(balance);
		}
	}
}

