package fi.uef.remotug.net.client;

public interface ConnectionListener {
	public void gameBalanceChanged(float balance);
	public void winnerAnnounced(int winnerID);
}
