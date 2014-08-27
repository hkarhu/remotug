package fi.uef.remotug.net.client;

public interface ConnectionListener {
	public void gameValuesChanged(float balance, float localForce, float remoteForce);
	public void readyAnnounced(int playerID);
	public void winnerAnnounced(int winnerID);
}
