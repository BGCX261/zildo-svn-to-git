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

package junit.area;

import junit.framework.Assert;
import junit.perso.EngineUT;

import org.junit.Test;

/**
 * @author Tchegito
 *
 */
public class CheckAltitude extends EngineUT {

	@Test
	public void voleurs() {
		mapUtils.loadMap("voleurs");

		mapUtils.displayAltitude();
		
		Assert.assertTrue(mapUtils.area.readAltitude(0, 38) == 0);
		Assert.assertTrue(mapUtils.area.readAltitude(0, 39) == 0);
		Assert.assertTrue(mapUtils.area.readAltitude(0, 40) == 0);
		Assert.assertTrue(mapUtils.area.readAltitude(0, 41) == 0);
		Assert.assertTrue(mapUtils.area.readAltitude(0, 42) == 0);
	}
}
