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

package zildo.monde.sprites.desc;

import zildo.client.gui.GUIDisplay;
import zildo.fwk.bank.SpriteBank;

/**
 * @author Tchegito
 *
 */
public enum FontDescription implements SpriteDescription {

	// Frame
	FRAME_CORNER_LEFT, FRAME_CORNER_RIGHT,
	// GUI icons
	GUI_HEART, GUI_HEARTEMPTY, GUI_RUPEE,
	GUI_BOMB, GUI_ARROW, GUI_KEY,
	// Number
	N_0, N_1, N_2, N_3, N_4, N_5, N_6, N_7, N_8, N_9, 
	// Life
	GUI_HEARTHALF,
	
	// Others
	GUI_GAUGE, GUI_AMOUNT, GUI_WEAPONFRAME,
	
	// Android
	VIRTUAL_PAD, BUTTON_X, BUTTON_Y;
	
	@Override
	public int getBank() {
		return SpriteBank.BANK_FONTES;
	}

	@Override
	public int getNSpr() {
		return ordinal() + 2 * GUIDisplay.transcoChar.length();
	}

	// All the following methods are nonsense for this kind of SpriteDescription
	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	public boolean isDamageable() {
		return false;
	}
	
	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isNotFixe() {
		return false;
	}
	
	@Override
	public boolean isSliping() {
		return true;
	}
}
