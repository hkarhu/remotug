package ropepull.gl;

import java.util.Vector;

import org.lwjgl.opengl.GL11;

public class Model {
	
	private final Vector<IndexPointer[]> polygons;
	private final Vector<float[]> vertCoordsContainer;
	private final Vector<float[]> texCoordsContainer;
	
	public Model(Vector<IndexPointer[]> polygons, Vector<float[]> vertCoords, Vector<float[]> texCoords) {
		this.polygons = polygons;
		this.vertCoordsContainer = vertCoords;
		this.texCoordsContainer = texCoords;
	}

	public void glDraw(){
		GL11.glBegin(GL11.GL_TRIANGLES);
		for (IndexPointer[] polygon : polygons){
			for(int i=0; i < 3; i++){
				float[] texCoords = texCoordsContainer.get(polygon[i].texIdx);
				float[] vertCoords = vertCoordsContainer.get(polygon[i].vertIdx);
				
				GL11.glTexCoord2f(texCoords[0], 1-texCoords[1]);
				GL11.glVertex3f(vertCoords[0]/10f, -vertCoords[1]/10f, -vertCoords[2]/10f);
			}
		}
		GL11.glEnd();
	}
	
	public static class IndexPointer {
		int vertIdx;
		int texIdx;
	}
	
}

