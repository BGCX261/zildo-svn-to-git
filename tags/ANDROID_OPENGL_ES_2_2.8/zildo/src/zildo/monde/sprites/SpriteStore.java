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

package zildo.monde.sprites;

import java.util.ArrayList;
import java.util.List;

import zildo.fwk.bank.SpriteBank;
import zildo.fwk.db.Identified;
import zildo.fwk.gfx.EngineFX;
import zildo.monde.sprites.desc.EntityType;
import zildo.monde.sprites.elements.Element;
import zildo.monde.sprites.persos.Perso;
import zildo.server.EngineZildo;

public class SpriteStore {

	static protected int n_bankspr;
	static protected List<SpriteBank> banque_spr = null;

	protected List<SpriteEntity> spriteEntities;

	static public final String[] sprBankName={"zildo.spr", 
											  "elem.spr", 
											  "pnj.spr", 
											  "font.spr", 
											  "pnj2.spr",
											  "gear.spr",
											  "pnj3.spr"};

	public SpriteStore() {
		
		// Load sprite banks
		if (banque_spr == null) {
			banque_spr=new ArrayList<SpriteBank>();
			n_bankspr=0;
			Identified.resetCounter(SpriteModel.class);
			for (int b=0;b<sprBankName.length;b++) {
				charge_sprites(sprBankName[b]);
			}
		}
		
		// Initialize entities list
		spriteEntities=new ArrayList<SpriteEntity>();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// charge_sprites
	///////////////////////////////////////////////////////////////////////////////////////
	public void charge_sprites(String filename)
	{
		SpriteBank sprBank=new SpriteBank();
	
		sprBank.charge_sprites(filename);
	
		banque_spr.add(sprBank);
		
		// Increase number of loaded banks
		n_bankspr++;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// getSpriteBank
	///////////////////////////////////////////////////////////////////////////////////////
	public SpriteBank getSpriteBank(int nBank)
	{
	    int bk=nBank;
	    if (nBank >= SpriteBank.BANK_ZILDOOUTFIT) {
	    	bk = SpriteBank.BANK_ZILDO;
	    }
	    return banque_spr.get(bk);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// spawnFont
	///////////////////////////////////////////////////////////////////////////////////////
	// IN:nBank, nSpr, x, y
	///////////////////////////////////////////////////////////////////////////////////////
	// Spawn a font character, same as spawnSprite, instead of :
	// -type is EntityType.FONT
	// -no alignment
	public SpriteEntity spawnFont(int nBank, int nSpr, int x, int y, boolean visible)
	{
	
		// SpriteEntity informations
		SpriteEntity entity=new SpriteEntity(x,y, false);
		entity.setScrX(x);
		entity.setScrY(y);
		entity.setNSpr(nSpr);
		entity.setNBank(nBank);
		entity.setForeground(true);	// Fonts are in front of the scene
	
		entity.setEntityType(EntityType.FONT);
	
		entity.setVisible(visible);
	
		entity.setSpecialEffect(EngineFX.NO_EFFECT);
	
		spawnSprite(entity);
	
		return entity;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// spawnSprite
	///////////////////////////////////////////////////////////////////////////////////////
	// IN:SpriteEntity object
	///////////////////////////////////////////////////////////////////////////////////////
	// Spawn a given SpriteEntity
	// -get the right Sprite object
	// -add resulted entity to the sprite engine
	public void spawnSprite(SpriteEntity entity)
	{
		int nBank=entity.getNBank();
		int nSpr=entity.getNSpr();
	
		SpriteModel spr;
		spr=getSpriteBank(nBank).get_sprite(nSpr);
	
		entity.setSprModel(spr);
	
		addSpriteEntities(entity);
	}
	
	/**
	 * Every addition in 'spriteEntities' list is done here. So subclasses can override this.
	 * @param p_entity
	 */
	protected void addSpriteEntities(SpriteEntity p_entity) {
		spriteEntities.add(p_entity);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// deleteSprite
	///////////////////////////////////////////////////////////////////////////////////////
	// IN:entity to destroy
	///////////////////////////////////////////////////////////////////////////////////////
	// -delete given sprite and linked entity
	///////////////////////////////////////////////////////////////////////////////////////
	public void deleteSprite(SpriteEntity entity)
	{
		if (entity != null) {
			spriteEntities.remove(entity);
			if (entity.getEntityType().isElement()) {
				Element element=(Element)entity;
				SpriteEntity linkedEntity=element.getLinkedPerso();
				// On regarde si cet �l�ment est li� � un autre �l�ment
				if (linkedEntity != null && linkedEntity.getEntityType().isElement()) {
					// Oui c'est le cas donc on supprime aussi l'autre �l�ment
					deleteSprite(element.getLinkedPerso());
				}
				element.finalize();
			} else if (entity.getEntityType().isPerso()) {
				Perso perso=(Perso)entity;
				EngineZildo.persoManagement.removePerso(perso);
				perso.finalize();
			}
			entity.visible=false;
			Identified.remove(SpriteEntity.class, entity.getId());
		}
	}
}