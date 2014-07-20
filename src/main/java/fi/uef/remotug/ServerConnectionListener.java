package fi.uef.remotug;

public interface ServerConnectionListener {
	public void gameBalanceChanged(float balance);
	public void winnerAnnounced(int winnerID);
}
