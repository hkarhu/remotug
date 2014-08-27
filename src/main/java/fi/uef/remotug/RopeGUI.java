package fi.uef.remotug;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.print.attribute.standard.MediaSize.Other;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import fi.conf.ae.gl.GLGraphicRoutines;
import fi.conf.ae.gl.GLValues;
import fi.conf.ae.gl.core.DisplayModePack;
import fi.conf.ae.gl.core.GLCore;
import fi.conf.ae.gl.core.GLKeyboardListener;
import fi.conf.ae.gl.text.GLBitmapFontBlitter;
import fi.conf.ae.gl.text.GLBitmapFontBlitter.Alignment;
import fi.conf.ae.gl.texture.GLTextureManager;
import fi.uef.remotug.gl.ModelManager;
import fi.uef.remotug.net.ReadyPacket;
import fi.uef.remotug.net.client.Connection;
import fi.uef.remotug.net.client.ConnectionListener;
import fi.uef.remotug.sensor.SensorListener;

public class RopeGUI extends GLCore implements GLKeyboardListener, ConnectionListener, SensorListener {

	private static final int ROUND_TIME = 30;
	
	private long startTime = 0;
	private long resetTime = 0;
	private volatile float balance = 0;
	private volatile float sensor = 0;
	private float lt = 0;
	private int winner = -1;
	private boolean localPlayerReady = false;
	private boolean otherPlayerReady = false;

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
		
		//Create new model manager instance
		new ModelManager().initialize();

		//Load all models
		ModelManager.getInstance().loadModel(this.getClass().getResourceAsStream("/map.rawgl"), "map");
		ModelManager.getInstance().loadModel(this.getClass().getResourceAsStream("/rope.rawgl"), "rope");
		
		startTime = System.currentTimeMillis();

		return true;

	}

	@Override
	public DisplayModePack glPickDisplayMode() throws Exception {return null;}

	@Override
	public void glLoop() {
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
        
		GLValues.cameraPositionX = (float) (6*Math.sin(0.25f*balance));
		GLValues.cameraPositionY = (float) (6*Math.cos(0.25f*balance));
		GLValues.cameraPositionZ = -9;
		GLValues.cameraRotationX = 0;
		GLValues.cameraRotationY = 0;
		GLValues.cameraRotationZ = -1;
		
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPushMatrix();
			GLTextureManager.getInstance().bindTexture("map");
			ModelManager.getInstance().getModel("map").glDraw();
			GLTextureManager.getInstance().bindTexture("rope_pos");
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
				remotePowerMeter.glDraw();
			GL11.glPopMatrix();
		
			GL11.glPushMatrix();
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glTranslatef(GLValues.glWidth*0.5f, GLValues.glHeight*0.5f, 0);
				GL11.glRotatef(-12, 0, 0, 1);
				GL11.glPushMatrix();
					GL11.glTranslatef(balance*(GLValues.glHeight*0.5f+GLValues.glWidth*0.5f)*0.5f, 0, 0);
					GLTextureManager.getInstance().bindTexture("rope_pos");
					GLGraphicRoutines.draw2DRect(-GLValues.glWidth, -0.0625f*GLValues.glHeight, GLValues.glWidth, 0.0625f*GLValues.glHeight, 0);
					GLTextureManager.getInstance().bindTexture("flag");
					GLGraphicRoutines.draw2DRect(-0.2f, -0.2f, 0.2f, 0.75f, 0f);
					GLTextureManager.unbindTexture();
					GL11.glColor4f(1, 0, 0, 1);
					GLGraphicRoutines.drawLine(0,-0.7f, 0, 1, 0, 0, 1.0f);
				GL11.glPopMatrix();

				GLTextureManager.unbindTexture();
				GL11.glColor4f(1, 0, 0, 1);
				GLGraphicRoutines.drawLine(0,-0.7f, 0, 1, -6, -6, 1.0f);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
				GL11.glTranslatef(GLValues.glWidth*0.19f, GLValues.glHeight*0.75f, -1);
				//GLBitmapFontBlitter.drawCircleString("Winner!   Winner!   Winner!   Winner!   ", 0.8f, 1, 1.25f + 0.3f*(float)(0.5f+0.5f*Math.sin(lt*0.002f)), lt*0.001f, "font");
				
				if(localPlayerReady){
					GL11.glColor4f(0.3f, 1, 0.3f, 1);
					GLBitmapFontBlitter.drawCircleString("Ready!   Ready!   Ready!   Ready!   ", 0.8f, 1, 1.25f + 0.3f*(float)(0.5f+0.5f*Math.sin(lt*0.002f)), lt*0.001f, "font");
				}
				localPowerMeter.glDraw();
			GL11.glPopMatrix();
			
//			GL11.glTranslatef(GLValues.glWidth/2, 0.5f, 5);
//			GLBitmapFontBlitter.drawString(String.format("%+.03f", balance), "font", 0.3f, 0.35f, Alignment.CENTERED);
//			
//			GL11.glTranslatef(0, GLValues.glHeight*0.8f, 0);
			
//			if(winner < 0){
//				GLBitmapFontBlitter.drawString(String.format("%.02f s", ROUND_TIME - (System.currentTimeMillis()-resetTime)*0.001f), "font", 0.3f, 0.35f, Alignment.CENTERED);
//			} else {
//				GLBitmapFontBlitter.drawString("Game Over", "font", 0.3f, 0.35f, Alignment.CENTERED);
//			}
			
			if(winner < 0){
				if(!connection.isConnected()){
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glTranslatef(GLValues.glWidth - ((lt%10000)*0.0001f)*GLValues.glWidth*3.5f, GLValues.glHeight*0.48f, -5);
					GLBitmapFontBlitter.drawScrollerString("Game Over                 Press space for rematch!", 0.3f, 1f, -4, 0.25f, lt*0.005f - 0.75f, "font");
				} else {
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glTranslatef(GLValues.glWidth*0.5f, GLValues.glHeight*0.48f, 0);
					GLBitmapFontBlitter.drawString("Disconnected", "font", 0.3f, 0.35f, Alignment.CENTERED);
				}
			}
			
		
		GL11.glPopMatrix();
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
			connection.writePacket(new ReadyPacket(Remotug.settings.getPlayerID()));
		}
	}
	
	@Override
	public void newSensorDataArrived(float kg) {
		localPowerMeter.setLocalForce(kg/500.0f);
	}

	@Override
	public void gameValuesChanged(float balance, HashMap<Integer, Float> balances) {
		this.balance = balance;
		localPowerMeter.setForce(balances.get(Remotug.settings.getPlayerID()));
		
		for(Entry<Integer, Float> e : balances.entrySet()){
			if(e.getKey() != Remotug.settings.getPlayerID()){
				remotePowerMeter.setForce(e.getValue());
			}
		}
	}

	@Override
	public void readyAnnounced(int playerID) {
		if(playerID == Remotug.settings.getPlayerID()){
			resetTime = System.currentTimeMillis();
			winner = -1;
			localPlayerReady = true;
		} else {
			otherPlayerReady = true;
		}
	}

	@Override
	public void winnerAnnounced(int winnerID) {
		winner = winnerID;
	}
	
	public void startAnnounced(long serverTime) {
		localPlayerReady = false;
		otherPlayerReady = false;
	}
	
	

}
