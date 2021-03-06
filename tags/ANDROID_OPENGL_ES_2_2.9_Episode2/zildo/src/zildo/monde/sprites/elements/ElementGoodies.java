/**
 * The Land of Alembrum
 * Copyright (C) 2006-2013 Evariste Boussaton
 * 
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

package zildo.monde.sprites.elements;

import zildo.fwk.script.logic.FloatExpression;
import zildo.monde.map.Tile.TileNature;
import zildo.monde.sprites.Reverse;
import zildo.monde.sprites.desc.ElementDescription;
import zildo.monde.sprites.persos.Perso;
import zildo.monde.sprites.persos.PersoZildo;
import zildo.server.EngineZildo;


public class ElementGoodies extends Element {

	// Coeur : nSpr=40
	// Diamant : nSpr=10
	
	private int timeToAcquire;	// Untakeable. Zildo has just to wait to have it (for chest)
	protected boolean volatil=true;	// TRUE=goodies disappear after a delay
	
	/**
	 * Common goodies : volatil
	 */
	public ElementGoodies() {
		super();
		spe=540;	// Goodies life duration, in frames (generally about 60FPS : 540==9sec)
	}
	
	/**
	 * Constructor for object coming from a chest. He's designed for Zildo.
	 * @param p_zildo
	 */
	public ElementGoodies(Perso p_zildo) {
		linkedPerso=p_zildo;
		timeToAcquire=60;
	}
	
	@Override
	public void animate() {
		
		TileNature nature = getCurrentTileNature();
		if (nature == null) {
			// If nature is null (probably out of the map), consider it's bottomless, to make the goodies disappear
			nature = TileNature.BOTTOMLESS;
		}
		switch (nature) {
			case BOTTOMLESS:
			case WATER:
				fall();
				dying = true;
				break;
		}
		super.animate();
		
		if (volatil) {
			spe--;
		}
		
		ElementDescription spr=ElementDescription.fromInt(nSpr);
		if (spr == ElementDescription.DROP_SMALL || spr == ElementDescription.DROP_MEDIUM) {
			// Coeur voletant vers le sol
			if (vx<=-0.15) {
				ax=0.01f;
				reverse = Reverse.NOTHING;
				addSpr=0;	// Coeur tourn� vers la gauche
			} else if (vx>=0.15) {
				ax=-0.01f;
				reverse = Reverse.HORIZONTAL;
				addSpr=1;	// Coeur tourn� vers la droite
			}
			if (z<=4) {
				nSpr=ElementDescription.DROP_FLOOR.ordinal();
				addSpr=0;
				vx=0;
				ax=0;
				y=y+3;
			} else if (z<=8) {
				nSpr = ElementDescription.DROP_MEDIUM.ordinal();
			}
		}
		
		
		if (spr==ElementDescription.DROP_FLOOR || spr.isMoney()) {
			// Il s'agit d'un diamant ou du coeur (10)
			int eff=EngineZildo.compteur_animation % 100;
			// 1) brillance
			if (eff<33 && spr!=ElementDescription.DROP_FLOOR) {		// Les diamants brillent
				addSpr=(eff / 10) % 3;
			}
			// 2) s'arr�te sur le sol
			if (z<4) {
				z=4;
			}
		}
		
		if (timeToAcquire > 0) {
			timeToAcquire--;
			if (timeToAcquire == 0) {
				// Zildo will now have the goodies
				int value = 0;
				if (name != null && name.length() != 0) {
					value = (int) new FloatExpression(name).evaluate(null);
				}
				if (((PersoZildo)linkedPerso).pickGoodies(this, value)) {
					dying=true;
				}
			}
		}
		
		if (spe==0) {
			// Le sprite doit mourir
			dying=true;
		} else if (spe<120) {
			visible=(spe%4>1);
		} else if (spe<60) {
			visible=(spe%2==0);
		}
		
		setAjustedX((int) x);
		setAjustedY((int) y);
	}
	
	@Override
	public boolean isGoodies() {
		return true;
	}
	
	@Override
	public boolean beingCollided(Perso p_perso) {
		return true;
	}

}
