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

package zildo.fwk.ui;

import zildo.client.sound.BankSound;

public abstract class ItemMenu {

	private  String text;
	public BankSound sound=BankSound.MenuSelect;
	private boolean launched;
	
	public ItemMenu() {
		
	}
	
	public ItemMenu(String p_text) {
		text=UIText.getMenuText(p_text);
	}

	public ItemMenu(String p_text, BankSound p_sound) {
		this(p_text);
		sound=p_sound;
	}

	public String getText() {
		return text;
	}
	
	/**
	 * Set an item name without bundle
	 * @param p_text
	 */
	public void setText(String p_text) {
		text=p_text;
	}
	
	public abstract void run();

	public boolean isLaunched() {
		return launched;
	}

	public void setLaunched(boolean launched) {
		this.launched = launched;
	}
		
}
