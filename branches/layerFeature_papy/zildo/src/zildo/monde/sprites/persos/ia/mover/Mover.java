/**
 * The Land of Alembrum
 * Copyright (C) 2006-2013 Evariste Boussaton
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

package zildo.monde.sprites.persos.ia.mover;

import java.util.HashMap;
import java.util.Map;

import zildo.monde.collision.Collision;
import zildo.monde.collision.DamageType;
import zildo.monde.sprites.SpriteEntity;
import zildo.monde.sprites.SpriteModel;
import zildo.monde.sprites.elements.Element;
import zildo.monde.sprites.persos.Perso;
import zildo.monde.util.Point;
import zildo.monde.util.Pointf;

/**
 * @author Tchegito
 *
 */
public class Mover {


	MoveOrder order;
	Map<Integer, SpriteEntity> linkedEntities;	// All entities carried by the mover
	SpriteEntity mobile;
	Element elemPlaceHolder;	// For physic mover
	
	/**
	 * @param mobile
	 * @param x
	 * @param y
	 */
	public Mover(SpriteEntity mobile) {
		this.mobile = mobile;
		this.linkedEntities = new HashMap<Integer, SpriteEntity>();
	}

	public void reachTarget() {
		Pointf delta = order.move();
		// Move the linked entities
		for (SpriteEntity entity : linkedEntities.values()) {
			// Handle collision for Perso (but is it really necessary ? Maybe for later !)
			if (entity.getEntityType().isPerso()) {
				Perso p = (Perso) entity;
				Pointf result = p.tryMove(delta.x, delta.y);
				p.x = result.x;
				p.y = result.y;
			} else {
				entity.x += delta.x;
				entity.y += delta.y;
			}
		}
	}
	
	public boolean isActive() {
		return order == null ? false : order.active;
	}
	
	/**
	 * Link an entity to this mover.
	 * @param e
	 * @return TRUE if entity is just newly associated (=it wasn't before)
	 */
	public boolean linkEntity(SpriteEntity e) {
		if (elemPlaceHolder != null && !e.getEntityType().isEntity()) {
			elemPlaceHolder.setLinkedPerso((Element) e);
		}
		return linkedEntities.put(e.getId(), e) == null;
	}
	
	/**
	 * Unlink an entity : it isn't on the moving entity anymore.
	 * @param e
	 */
	public void unlinkEntity(SpriteEntity e) {
		linkedEntities.remove(e.getId());
	}
	
	/**
	 * Returns TRUE if the given entity is on the mover.
	 * @param e entity
	 * @return boolean
	 */
	public boolean isOnIt(SpriteEntity e) {
		return linkedEntities.containsKey(e.getId());
	}
	
	Element getPlaceHolder() {
		if (elemPlaceHolder == null) {
			elemPlaceHolder = new Element() {
				@Override
				public Collision getCollision() {
					SpriteModel spr = mobile.getSprModel();
					Point pCenter = new Point(mobile.x, mobile.y); // + spr.getTaille_y() / 2);
					Point size = new Point(spr.getTaille_x(), spr.getTaille_y()).multiply(0.7f); 
					Collision c = new Collision(pCenter, size, null, DamageType.HARMLESS, null);
					return c;
				}
			};
		}
		return elemPlaceHolder;
	}
	
	public void merge(MoveOrder m) {
		order = m;
		order.init(this);
	}
	
}
