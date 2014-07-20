package fi.uef.remotug.net.server;

import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.TestPacket;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

public class ServerHandler extends ChannelHandlerAdapter {
	
	private ChannelGroup allClients;

	public ServerHandler(ChannelGroup allClients) {
		this.allClients = allClients;
	}
	
//	-- echo server protocol:
//	@Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		allClients.add(ctx.channel());
//		
//		// -- welcome!
//		
//		ByteBuf buf = ctx.alloc().buffer();
//		buf.writeBytes(new String("Welcome!\n").getBytes(StandardCharsets.UTF_8));
//		ctx.channel().writeAndFlush(buf);
//		
//		/* -- bytebuf reference counting
//		 * correct? or sent items are released by netty? src:
//		 * ahh, ok: "When an outbound (a.k.a. downstream) message reaches at the beginning of the pipeline,
//		 * 			 Netty will release it after writing it out."
//		 * http://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-11
//		 * http://netty.io/wiki/reference-counted-objects.html
//		 */
//		//buf.release();
//	}
//	
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		ctx.writeAndFlush(msg);
//	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[server] client connected");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("[server] client data");
		BasePacket p;
		
		try {
			p = (BasePacket) msg;
		} catch (ClassCastException e) {
			System.err.println("Received invalid packet! Disconnected client.");
			ctx.channel().close();
			return;
		}
		
		if (p.getClass() == TestPacket.class) {
			String c = ((TestPacket) p).getContent();
			System.out.println("Received TestPacket. Content: ["+ c+ "]");
					
		}/* else if (p.getClass() == AnotherPacketType.class) {
			...
					
		}*/ else {
			System.err.println("Received valid base packet but unknown class type! Client newer than server? Disconnected client.");
			ctx.channel().close();
			return;
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
}
