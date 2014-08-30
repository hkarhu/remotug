package fi.uef.remotug;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.print.attribute.standard.MediaSize.Other;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import fi.conf.ae.gl.GLGraphicRoutines;
import fi.conf.ae.gl.GLValues;
import fi.conf.ae.gl.core.DisplayModePack;
import fi.conf.ae.gl.core.GLCore;
import fi.conf.ae.gl.core.GLKeyboardListener;
import fi.conf.ae.gl.text.GLBitmapFontBlitter;
import fi.conf.ae.gl.text.GLBitmapFontBlitter.Alignment;
import fi.conf.ae.gl.texture.GLTextureManager;
import fi.conf.ae.gl.texture.GLTextureRoutines;
import fi.conf.ae.routines.S;
import fi.uef.remotug.gl.ModelManager;
import fi.uef.remotug.net.ReadyPacket;
import fi.uef.remotug.net.client.Connection;
import fi.uef.remotug.net.client.ConnectionListener;
import fi.uef.remotug.sensor.SensorListener;

public class RopeGUI extends GLCore implements GLKeyboardListener, ConnectionListener, SensorListener {

    private final float SCALER = 2280f;
    
    private int matchDuration = 30;
	private long startTime = 0;
	private long endTime = 0;
	private volatile float balance = 0;
	private volatile float sensor = 0;
	private float lt = 0;
	private int winner = -1;
	private boolean localPlayerReady = false;
	private boolean remotePlayerReady = false;
	private boolean gameOn = false;
	private String remotePlayerName = "";
	
	private LocalPowerMeter localPowerMeter = new LocalPowerMeter();
	private PowerMeter remotePowerMeter = new PowerMeter();
	
	private final Connection connection;
	
	public RopeGUI(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public boolean glInit() {

		GL11.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f);

		//Some nice values
		GL11.glEnable( GL11.GL_ALPHA_TEST );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glDepthFunc( GL11.GL_LEQUAL );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_NORMALIZE);
		GL11.glEnable( GL11.GL_CULL_FACE );
		GL11.glCullFace( GL11.GL_BACK );
		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL11.GL_DITHER );
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		//Sharp bitmap scaling
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		
		//Clear buffers
		GL11.glClear(
				GL11.GL_COLOR_BUFFER_BIT |
				GL11.GL_DEPTH_BUFFER_BIT |
				GL11.GL_ACCUM_BUFFER_BIT |
				GL11.GL_STENCIL_BUFFER_BIT
				);
		
		//Add this as a listener to keyboard and mouse data providers
		keyboardListeners.add(this);
		
		//Create new instance for texture manager
		new GLTextureManager(getExecutorService()).initialize();
		
		//Load all graphics TODO:kaikki loopissa
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/font_default.png"), "font");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/map.png"), "map");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/rope.png"), "rope");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/meter_indicator.png"), "meter_indicator");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/meter_face.png"), "meter_face");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/rope_pos.png"), "rope_pos");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/flag.png"), "flag");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/win.png"), "win");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/lose.png"), "lose");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/meter_local_indicator.png"), "meter_local_indicator");
		
		//Create new model manager instance
		new ModelManager().initialize();

		//Load all models
		ModelManager.getInstance().loadModel(this.getClass().getResourceAsStream("/map.rawgl"), "map");
		ModelManager.getInstance().loadModel(this.getClass().getResourceAsStream("/rope.rawgl"), "rope");
		
		startTime = System.currentTimeMillis();

		Display.setVSyncEnabled(true);
		Display.sync(50);
		
		return true;

	}

	@Override
	public DisplayModePack glPickDisplayMode() throws Exception {return null;}

	@Override
	public void glLoop() {
		
		GL11.glClearColor( 0.15f, 0.15f, 0.15f, 1.0f);
		
		GL11.glClear(
				GL11.GL_COLOR_BUFFER_BIT |
				GL11.GL_DEPTH_BUFFER_BIT |
				GL11.GL_ACCUM_BUFFER_BIT |
				GL11.GL_STENCIL_BUFFER_BIT
				);

		lt = (System.currentTimeMillis() - startTime)%Float.MAX_VALUE;

		//Init perspective projection and draw the map
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, 0);
		GL11.glEnable(GL11.GL_LIGHTING);
		GLGraphicRoutines.initPerspective(60);
		GLGraphicRoutines.initCamera();

		//Light
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        fb.put(new float[] {0.0f, 0.0f, 0.0f, 1.0f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, fb);
        fb.put(new float[] {1.0f, 1.0f, 1.0f, 0.5f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, fb);
        fb.put(new float[] {1.0f, 1.0f, 1.0f, 0.25f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, fb);
        fb.put(new float[] {0.0f, 0.0f, 10.0f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, fb);
        fb.clear();
        
        GL11.glEnable(GL11.GL_LIGHT0);
        
		GLValues.cameraPositionX = (float)Math.sin(lt*0.0002f)*0.1f + 1;
		GLValues.cameraPositionY = (float)Math.cos(Math.cos(lt*0.0002f))*0.2f + 1f;
		GLValues.cameraPositionZ = -7f + (float)Math.sin(lt*0.0001f)*0.5f;
		GLValues.cameraTargetX = 0;
		GLValues.cameraTargetY = 0;
		GLValues.cameraTargetZ = 0;
		GLValues.cameraRotationX = 0.1f;
		GLValues.cameraRotationY = 0;
		GLValues.cameraRotationZ = -1;
		
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glColor4f(0.6f, 0.6f, 0.6f, 1);
		GL11.glPushMatrix();
			GL11.glTranslatef(-0.1f, -0.1f, 0);
			GLTextureManager.getInstance().bindTexture("map");
			ModelManager.getInstance().getModel("map").glDraw();
			GLTextureManager.getInstance().bindTexture("rope");
			ModelManager.getInstance().getModel("rope").glShiftDraw(balance*5);
		GL11.glPopMatrix();

		//Swap to orthogonal projection
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, 0);
		GLGraphicRoutines.initOrtho();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor4f(1, 1, 1,1);
		
		GL11.glPushMatrix();
	
			GL11.glPushMatrix();
				GL11.glTranslatef(GLValues.glWidth*0.82f, GLValues.glHeight*0.22f, 1);
				GL11.glScalef(1, 1, 1);
				if(remotePlayerReady){
					drawReady();
				}
				remotePowerMeter.glDraw();
			GL11.glPopMatrix();
		
			GL11.glPushMatrix();
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glTranslatef(GLValues.glWidth*0.5f, GLValues.glHeight*0.5f, 0);
				GL11.glRotatef(-12, 0, 0, 1);
				GL11.glPushMatrix();
					GL11.glTranslatef(-balance*(GLValues.glHeight*0.5f+GLValues.glWidth*0.5f)*0.5f, 0, 0);
					GLTextureManager.getInstance().bindTexture("rope_pos");
					GLGraphicRoutines.draw2DRect(-GLValues.glWidth, -0.0625f*GLValues.glHeight, GLValues.glWidth, 0.0625f*GLValues.glHeight, 0);
					GLTextureManager.getInstance().bindTexture("flag");
					GLGraphicRoutines.draw2DRect(-0.2f, -0.2f, 0.2f, 0.75f, 0f);
					GLTextureManager.unbindTexture();
					GL11.glColor4f(1, 0, 0, 1);
					GLGraphicRoutines.drawLine(0,-0.7f, 0, 1, 0, 0, 1.0f);
				GL11.glPopMatrix();

				GLTextureManager.unbindTexture();
				GL11.glColor4f(1, 1, 1, 1);
				GLGraphicRoutines.drawLine(0,-0.7f, 0, 1, -6, -6, 1.0f);
				
				GL11.glPushMatrix();
					GL11.glTranslatef(-GLValues.glWidth*0.3f, -0.3f,0);
					GLBitmapFontBlitter.drawString(Remotug.settings.getPlayerName(), "font", 0.2f, 0.25f, Alignment.CENTERED);
				GL11.glPopMatrix();
				
				GL11.glPushMatrix();
					GL11.glTranslatef(GLValues.glWidth*0.33f, 0.3f,0);
					GLBitmapFontBlitter.drawString(remotePlayerName, "font", 0.2f, 0.25f, Alignment.CENTERED);
				GL11.glPopMatrix();
				
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
				GL11.glTranslatef(GLValues.glWidth*0.19f, GLValues.glHeight*0.75f, -1);
				if(localPlayerReady){
					drawReady();
				}
				localPowerMeter.glDraw();
			GL11.glPopMatrix();
			
			GL11.glColor4f(1, 1, 1, 1);
			
			GL11.glPushMatrix();
				GL11.glTranslatef(GLValues.glWidth*0.5f, GLValues.glHeight*0.93f, GLValues.glDepth*0.5f);
				if(gameOn && System.currentTimeMillis() < endTime) {
					if(System.currentTimeMillis() > endTime-5000){
						GL11.glColor4f(1, 0, 0, 0.5f+(float)Math.sin(lt*0.05f));	
					}
					GLBitmapFontBlitter.drawString("" + (int)((endTime-System.currentTimeMillis())/1000.0f + 1), "font", 0.3f, 0.4f, Alignment.CENTERED);
				}
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			
				if(!gameOn){
					
					GL11.glTranslatef(0, 0, GLValues.glDepth);
					
					if(!localPlayerReady && !remotePlayerReady){
						GLTextureManager.unbindTexture();
						GL11.glColor4f(0, 0, 0, 0.6f);
						GLGraphicRoutines.draw2DRect(0, 0, GLValues.glWidth, GLValues.glHeight, 0);
					}
					
					GL11.glTranslatef(GLValues.glWidth*0.5f, GLValues.glHeight*0.48f, 0);
					GL11.glColor4f(1, 1, 1, 1);
					
					if(winner >= 0){ //Won
						if(winner == Remotug.settings.getPlayerID()){
							GLTextureManager.getInstance().bindTexture("win");
							GL11.glRotatef(20*(float)Math.sin(lt*0.003f), 0, 0, 1);
							GLGraphicRoutines.draw2DRect(-1.5f, -0.75f, 1.5f, 0.75f, 0);
							GLBitmapFontBlitter.drawString("You win!", "font", 0.4f, 0.7f, Alignment.CENTERED);
						} else { //Lost
							GL11.glTranslatef(0, -0.2f, 0);
							GLTextureManager.getInstance().bindTexture("lose");
							GL11.glTranslatef(0.3f*(float)Math.sin(lt*0.003f), 0, 0);
							GL11.glRotatef(10*(float)Math.sin(lt*0.003f), 0, 0, 1);
							GLGraphicRoutines.draw2DRect(-1.5f, -0.75f, 1.5f, 0.75f, 0);
							GL11.glTranslatef(0, 0.15f, 0);
							GLBitmapFontBlitter.drawString("You lost", "font", 0.2f, 0.4f, Alignment.CENTERED);
						}
					} else if(localPlayerReady && remotePlayerReady){
						GLTextureManager.unbindTexture();
						GL11.glColor4f(0, 0, 0, 0.6f);
						GLGraphicRoutines.draw2DRect(-GLValues.glWidth*0.5f, -0.3f, GLValues.glWidth*0.5f, 0.3f, 0);
						GL11.glColor4f(1, 1, 1, 1);
						GLBitmapFontBlitter.drawString("Get ready! ", "font", 0.3f, 0.45f, Alignment.CENTERED);
					}
					
				}
			GL11.glPopMatrix();
			
			if(connection.isConnected()){
				if(!gameOn && !localPlayerReady){
					//Game over 
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glTranslatef(GLValues.glWidth - ((lt%10000)*0.0001f)*GLValues.glWidth*3.75f, GLValues.glHeight*0.08f, GLValues.glDepth);
					GLBitmapFontBlitter.drawScrollerString("Game Over                 Press space to begin a new match!", 0.3f, 0.5f, -3, 0.25f, lt*0.005f - 0.75f, "font");
				}
			} else {
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glTranslatef(GLValues.glWidth*0.5f, GLValues.glHeight*0.48f, 0);
				GLBitmapFontBlitter.drawString("Disconnected", "font", 0.3f, 0.35f, Alignment.CENTERED);
			}		
		
		GL11.glPopMatrix();
		
	}

	private void drawReady(){
		GL11.glColor4f(0, 0.6f, 0, 0.4f);
		GLTextureManager.unbindTexture();
		GLGraphicRoutines.drawCircle(1.25f + 0.3f*(float)(0.5f+0.5f*Math.sin(lt*0.002f)), 30);
		GL11.glColor4f(0, 1, 0, 1);
		GLGraphicRoutines.drawLineCircle(1.25f + 0.3f*(float)(0.5f+0.5f*Math.sin(lt*0.002f)), 30, 1.0f);
		GLBitmapFontBlitter.drawCircleString("Ready!   Ready!   Ready!   Ready!   ", 0.8f, 1, 1.25f + 0.3f*(float)(0.5f+0.5f*Math.sin(lt*0.002f)), lt*0.001f, "font");
	}
	
	@Override
	public void glFocusChanged(boolean isFocused) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTerminate() {
		//Shut down the texture processor treads
		GLTextureManager.getInstance().requestShutdown();
	}

	@Override
	public void glKeyDown(int eventKey, char keyChar) {}

	@Override
	public void glKeyUp(int eventKey, char keyChar) {
		//Reset game
		if(eventKey == Keyboard.KEY_SPACE){
			connection.writePacket(new ReadyPacket(Remotug.settings.getPlayerID(), Remotug.settings.getPlayerName()));
		} else if(eventKey == Keyboard.KEY_ESCAPE){
			Remotug.shutdown();
		}
	}
	
	@Override
	public void newSensorDataArrived(float kg) {
		localPowerMeter.setLocalForce(kg/SCALER);
	}

	@Override
	public void gameValuesChanged(float balance, HashMap<Integer, Float> balances) {
		if(balances == null || balances.isEmpty()) return;
		
		if(gameOn){
			this.balance = balance;
		}
		
		//System.out.println("Balance: " + balance);
		
		if(balances.containsKey(Remotug.settings.getPlayerID())){
			localPowerMeter.setForce(balances.get(Remotug.settings.getPlayerID())/SCALER);
		} else {
			System.out.println("No my key in hashmap, " + Remotug.settings.getPlayerID());
			return;
		}
		
		for(Entry<Integer, Float> e : balances.entrySet()){
			if(e.getKey() != Remotug.settings.getPlayerID()){
				remotePowerMeter.setForce(e.getValue()/SCALER);
			}
			//System.out.println("Hasmap has: " + e.getKey() + " " + e.getValue());
		}
	}

	@Override
	public void readyAnnounced(int playerID, String remoteName) {
		if(playerID == Remotug.settings.getPlayerID()){
			winner = -1;
			balance = 0;
			localPlayerReady = true;
		} else {
			remotePlayerReady = true;
			remotePlayerName = remoteName;
		}
	}

	@Override
	public void winnerAnnounced(int winnerID) {
		System.out.println("Winner?");
		winner = winnerID;
		gameOn = false;
	}
	
	@Override
	public void startAnnounced(long serverTime, int duration, int delay) {
		localPlayerReady = false;
		remotePlayerReady = false;
		gameOn = true;
		endTime = System.currentTimeMillis() + duration;
		
		Remotug.settings.ebinStart();
	}
	

}
