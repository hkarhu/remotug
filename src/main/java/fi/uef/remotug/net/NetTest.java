package fi.uef.remotug.net;

import java.io.IOException;

import fi.uef.remotug.net.client.RemoteClient;
import fi.uef.remotug.net.server.RemotugServer;

public class NetTest {
	
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
