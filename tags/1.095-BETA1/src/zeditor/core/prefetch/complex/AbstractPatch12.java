/**
 * Legend of Zildo
 * Copyright (C) 2006-2011 Evariste Boussaton
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

package zeditor.core.prefetch.complex;

import java.util.List;

import zildo.fwk.MultiMap;
import zildo.monde.map.Area;
import zildo.monde.map.Point;

/**
 * Abstract class which provides a smart drawing from a patch designed with 12 different tiles.<br/>
 * 
 * All derived classes must have at least two overriding methods:<ul>
 * <li>toBinaryValue : convert graphical to 0..15 value</li>
 * <li>toGraphicalValue : convert 0..15 value to displayable one</li>
 * </ul>
 * @author Tchegito
 *
 */
public abstract class AbstractPatch12 extends TraceDelegateDraw {

	int[] smallPatch = new int[] { 8, 4, 2, 1};
	int[] bigPatch = new int[] {8, 12, 4, 10, 15, 5, 2, 3, 1};

	boolean big;
	
	public AbstractPatch12(boolean p_big) {
		big = p_big;
	}

	/**
	 * Returns a 0..15 value from a tile number.
	 * @param p_val
	 * @return int
	 */
	abstract int toBinaryValue(int p_val);

	/**
	 * Returns a tile number from a 0..15 value.
	 * @param p_val
	 * @return int
	 */
	abstract int toGraphicalValue(int p_val);
		
	@Override
	/**
	 * Draw a patch on the map, and arrange existing drawing.
	 */
	public void draw(Area p_map, Point p_start) {
		draw(p_map, p_start, null);
	}
	
	public void draw(Area p_map, Point p_start, CompositePatch12 p_composite) {
		int size=2;
		
		if (big) {
			size = (int) Math.sqrt(bigPatch.length);
		}
		for (int i=0;i<size;i++) {
			for (int j=0;j<size;j++) {
				int patchValue;
				if (big) {
					patchValue = bigPatch[i*size +j];
				} else {
					patchValue = smallPatch[i*size +j];
				}
				if (patchValue != -1) {
				    arrangeOneTile(p_map, patchValue, p_start.x+j, p_start.y+i, p_composite);
				}
			}
		}
	}
	
	protected void arrangeOneTile(Area p_map, int p_patchValue, int p_x, int p_y, CompositePatch12 p_composite) {
		// Get map value
		int val=p_map.readmap(p_x, p_y);
		if (p_composite != null && !p_composite.canDraw(val)) {
			return;
		}
		val=toBinaryValue(val);
		// Add patch value
		val=val | p_patchValue;
		// And render the arranged one
		val=toGraphicalValue(val); 
		drawOneTile(p_map, p_x, p_y, val);		
	}
	
	public void drawOneTile(Area p_map, int p_x, int p_y, int p_val) {
		p_map.writemap(p_x, p_y, p_val);
	}

	final protected void setBigPatch(int[] p_bytes) {
		bigPatch = p_bytes;
	}
	
	final protected int[] getBigPatch() {
	    return bigPatch;
	}
	
	final protected byte[] getReverseTab(byte[] p_tab, int p_startTile) {
	    MultiMap<Integer, Integer> temp=new MultiMap<Integer, Integer>();
	    int maxTile=0;
	    for (int i=0;i<=15;i++) {
		temp.put((int) p_tab[i], i);
		maxTile = Math.max(p_tab[i], maxTile);
	    }
	    byte[] result = new byte[maxTile - p_startTile + 1];
	    for (int i=p_startTile;i<=maxTile;i++) {
		List<Integer> vals = temp.get(i);
		if (vals != null) {
        		for (Integer v : vals) {
        		    result[i-p_startTile] = v.byteValue();
        		}
		} else {
		    result[i-p_startTile] = 0;
		}
	    }
	    
	    return result;
	}
	
	/**
	 * Returns TRUE if the given value is apart of this patch.
	 * @param p_val
	 * @return boolean
	 */
	boolean isFromThis(int p_val) {
		int a = toBinaryValue(p_val);
		return a != 0;
	}
}
