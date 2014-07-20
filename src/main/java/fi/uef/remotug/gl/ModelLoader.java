package fi.uef.remotug.gl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.Vector;

import fi.conf.ae.AE;
import fi.conf.ae.routines.S;
import fi.uef.remotug.gl.Model.IndexPointer;

public class ModelLoader {
	
	public static Model loadModel(InputStream inStream) {
		try {
			return _loadModel(inStream);
		}
		catch (IOException e) {
			new Exception(S.sprintf("Failed to preprocess model 'failed to open input Path': %s", e)).printStackTrace();
			return null;
		}
	}

	private static Model _loadModel(InputStream inStream) throws IOException {

		Vector<IndexPointer[]> polygons; // ids
		Vector<float[]> vertCoords; // actual vert coord data
		Vector<float[]> texCoords; // actual tex coord data
		
		polygons = new Vector<>();
		vertCoords = new Vector<>();
		texCoords = new Vector<>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		
		int numUniqueTexCoords=0;
		int numUniqueVertCoords=0;
		
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				Scanner s = new Scanner(line);
				// s.useDelimiter("[,#;\\n$]");
				s.useDelimiter("[,#;]");
				
				IndexPointer[] indexPointer = new IndexPointer[] { new IndexPointer(), new IndexPointer(), new IndexPointer() };
				polygons.add(indexPointer);
				
				float tx, ty, vx, vy, vz;
				for (int i=0; i<3; i++) {
					int idx;
					
					// tex coords
					tx = Float.parseFloat(s.next());
					ty = Float.parseFloat(s.next());
					
					idx = findExistingTexCoord(tx, ty, texCoords);
					if (idx == -1) {
						// add new coord
						texCoords.add(new float[] { tx, ty });
						indexPointer[i].texIdx = texCoords.size() - 1;
						numUniqueTexCoords++;
					}
					else {
						// use looked up idx
						indexPointer[i].texIdx = idx;
					}
					
					// vert coords
					vx = Float.parseFloat(s.next());
					vy = Float.parseFloat(s.next());
					vz = Float.parseFloat(s.next());
					
					idx = findExistingVertCoord(vx, vy, vz, vertCoords);
					if (idx == -1) {
						// add new coord
						vertCoords.add(new float[] { vx, vy, vz });
						indexPointer[i].vertIdx = vertCoords.size() - 1;
						numUniqueVertCoords++;
					}
					else {
						// use looked up idx
						indexPointer[i].vertIdx = idx;
					}
				}
			} // while
			
			if (AE.isDebug()) {
				S.debugFunc("Read (unique) %d vertex coords and %d texture coords", numUniqueVertCoords, numUniqueTexCoords);
			}
			
			return new Model(polygons, vertCoords, texCoords);
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	private static int findExistingTexCoord(float tx, float ty, Vector<float[]> texCoords) {
		int idx = -1;
		for (float[] texCoord : texCoords) {
			idx++;
			if (texCoord[0] != tx) continue;
			if (texCoord[1] != ty) continue;
			return idx;
		}
		return -1;
	}

	private static int findExistingVertCoord(float vx, float vy, float vz, Vector<float[]> vertCoords) {
		int idx = -1;
		for (float[] vertCoord : vertCoords) {
			idx++;
			if (vertCoord[0] != vx) continue;
			if (vertCoord[1] != vy) continue;
			if (vertCoord[2] != vz) continue;
			return idx;
		}
		return -1;
	}
	
}
