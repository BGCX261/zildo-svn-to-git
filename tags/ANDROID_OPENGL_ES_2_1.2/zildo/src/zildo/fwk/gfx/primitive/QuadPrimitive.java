/**
 * Legend of Zildo
 * Copyright (C) 2006-2012 Evariste Boussaton
 * Based on original Zelda : link to the past (C) Nintendo 1992
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zildo.fwk.gfx.primitive;

import zildo.Zildo;
import zildo.fwk.opengl.compatibility.VBO;
import zildo.fwk.opengl.compatibility.VBOBuffers;
import zildo.monde.sprites.Reverse;
import zildo.monde.sprites.Rotation;

/**
 * @author Tchegito
 *
 */
public class QuadPrimitive {

    // Class variables
    protected int nPoints;
    protected int nIndices;
    
    private boolean isLock;

    protected VBOBuffers bufs;
    protected VBO vbo;
    
    private int textureSizeX = 256;
    private int textureSizeY = 256;

    public QuadPrimitive(int numPoints) {
    	initialize(numPoints);
    }
    
    public QuadPrimitive(int numPoints, int texSizeX, int texSizeY) {
        textureSizeX = texSizeX;
        textureSizeY = texSizeY;
        initialize(numPoints);
    }
    
    protected void initialize(int numPoints) {
        // Initialize VBO IDs
    	vbo = Zildo.pdPlugin.gfxStuff.createVBO();
        bufs=vbo.create(numPoints, false);
        
        nPoints = 0;
        nIndices = 0;
    }
    
    // /////////////////////////////////////////////////////////////////////////////////////
    // startInitialization
    // /////////////////////////////////////////////////////////////////////////////////////
    // Lock VertexBuffer to gain access to data
    // /////////////////////////////////////////////////////////////////////////////////////
    public void startInitialization() {
    	nPoints = 0;
    	nIndices = 0;
    }

    // /////////////////////////////////////////////////////////////////////////////////////
    // endInitialization
    // /////////////////////////////////////////////////////////////////////////////////////
    public void endInitialization() {
        vbo.endInitialization(bufs);
    }
    
    public void cleanUp() {
       	vbo.cleanUp(bufs);
        nPoints = 0;
        nIndices = 0;
    }
    
    public boolean isLock() {
        return isLock;
    }

    /**
     *  Move a tile and reset its texture (don't change size)<br/>
     * {@link #startInitialization()} should be called first.
     * @param x
     * @param y
     * @param u
     * @param v
     * @param reverse reverse attribute (horizonta and/or vertical)
     */
    public void updateQuad(float x, float y, float u, float v, Reverse reverse) {
        // Get size
        int vBufferPos = bufs.vertices.position(); // - 3*4;
		int tBufferPos = bufs.textures.position(); // - 2*4;

        if (bufs.vertices.limit() <= vBufferPos) {
            // On rajoute une place
            bufs.vertices.limit(vBufferPos + 2 * 4);
        }
		if (bufs.textures.limit() <= tBufferPos) {
			bufs.textures.limit(tBufferPos + 2 * 4);
		}
        
        float sizeX = bufs.vertices.get(vBufferPos + 2) - bufs.vertices.get(vBufferPos);
        float sizeY = bufs.vertices.get(vBufferPos + 2 * 2 + 1) - bufs.vertices.get(vBufferPos + 1);

        if (sizeX == 0) {
        	sizeX=16;
        	sizeY=16;
        }
		
		int revX = reverse.isHorizontal() ? -1 : 1;
		int revY = reverse.isVertical() ? -1 : 1;
		
        // Move tile
        addSprite(x, y, u, v, sizeX * revX, sizeY * revY, Rotation.NOTHING);

    }
    
    float[][] texCoords = new float[4][2];

    protected void putTexture(float xTex, float yTex, float sizeX, float sizeY) {
        float texStartX=xTex;
        float texStartY=yTex;
        if (sizeX < 0) {
        	texStartX-=sizeX;
        }
        if (sizeY < 0) {
        	texStartY-=sizeY;
        }
        for (int i = 0; i < 4; i++) {
            // Get right tile-texture
            float texPosX = texStartX + sizeX * (i % 2);
            float texPosY = texStartY + sizeY * (i / 2);

            texCoords[i][0] = texPosX / textureSizeX;
            texCoords[i][1] = texPosY / textureSizeY;
        }
        bufs.textures.put(texCoords[0][0]).put(texCoords[0][1]);
        bufs.textures.put(texCoords[1][0]).put(texCoords[1][1]);
        bufs.textures.put(texCoords[2][0]).put(texCoords[2][1]);
        bufs.textures.put(texCoords[1][0]).put(texCoords[1][1]);
        bufs.textures.put(texCoords[3][0]).put(texCoords[3][1]);
        bufs.textures.put(texCoords[2][0]).put(texCoords[2][1]);
    }

    // Return the quad position in Vertex Buffer
    protected int addQuadSized(int x, int y, float xTex, float yTex, int sizeX, int sizeY) {
        //putQuadSized(x, y, sizeX, sizeY, xTex, yTex);
        addSprite(x, y, xTex, yTex, sizeX, sizeY, Rotation.NOTHING);
        
        return nPoints - 4;
    }

    short[][] vertices = new short[4][2];
    byte[][] orders = {{0, 1, 2, 3}, {2, 0, 3, 1}, {1, 3, 0, 2}, {3, 2, 1, 0}};
    
    protected void addSprite(float x, float y, float xTex, float yTex, float sizeX, float sizeY, Rotation rotation ) {
    	
        // 4 bufs.vertices
        if (bufs.vertices.position() == bufs.vertices.limit()) {
            // On rajoute une place
            bufs.vertices.limit(bufs.vertices.position() + 2 * 6);
            bufs.textures.limit(bufs.textures.position() + 2 * 6);
        }
        float pixSizeX=Math.abs(sizeX);
        float pixSizeY=Math.abs(sizeY);
        
        if (rotation != Rotation.NOTHING) {
        	if (rotation != Rotation.UPSIDEDOWN) {
        		float siz = pixSizeX;
        		pixSizeX = pixSizeY;
        		pixSizeY = siz;
        	}
        }
        for (int i = 0; i < 4; i++) {
        	vertices[orders[rotation.value][i]][0] = (short) (x + pixSizeX * (i % 2));	// x
        	vertices[orders[rotation.value][i]][1] = (short) (y + pixSizeY * (i / 2));	// y
        }
        
        bufs.vertices.put(vertices[0][0]).put(vertices[0][1]);
        bufs.vertices.put(vertices[1][0]).put(vertices[1][1]);
        bufs.vertices.put(vertices[2][0]).put(vertices[2][1]);
        bufs.vertices.put(vertices[1][0]).put(vertices[1][1]);
        bufs.vertices.put(vertices[3][0]).put(vertices[3][1]);
        bufs.vertices.put(vertices[2][0]).put(vertices[2][1]);

        putTexture(xTex, yTex, sizeX, sizeY);
        
        nPoints += 4;
        nIndices += 6;
    }
    
    /**
     * Ask OpenGL to render every quad from this mesh.
     */
    public void render() {

        // Indices buffer contains indices for 4096 tiles. We have to limit it to the real number of used tiles.
        if (bufs.indices != null) {
        	bufs.indices.limit(nIndices);
            vbo.draw(bufs);
        } else {
        	vbo.draw(bufs, 0, nIndices);
        }


    }
    
    public boolean isEmpty() {
        return nPoints == 0;
    }
}
