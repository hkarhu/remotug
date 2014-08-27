package fi.uef.remotug;

import org.lwjgl.opengl.GL11;

import fi.conf.ae.gl.GLGraphicRoutines;
import fi.conf.ae.gl.texture.GLTextureManager;

public class LocalPowerMeter extends PowerMeter {
	
	private volatile float localForce = 0;
	
	public void glDraw(){
		
		GL11.glPushMatrix();
		//GL11.glScalef(1,-1, 1);
		super.glDraw();
		
		GL11.glRotatef(105 + 330*localForce, 0, 0, 1);
		GL11.glColor4f(1,1,1,0.5f);
		GLTextureManager.getInstance().bindTexture("meter_indicator");
		GLGraphicRoutines.draw2DRect(-0.12f*METER_SIZE, -0.12f*METER_SIZE, 0.8f*METER_SIZE, 0.12f*METER_SIZE, 0);
		GL11.glPopMatrix();
		
		//localForce = (float)(Math.sin((System.currentTimeMillis()-1409129465301l)*0.0002f)*0.5f+0.5f);
	}
	
	public void setLocalForce(float localForce){
		this.localForce = localForce;
	}

}
