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

package zildo.fwk.gfx.filter;

import zildo.client.PlatformDependentPlugin;

/**
 * References all declared filters. It's important to have the exhaustivity here, because the
 * injection framework browses all these classes (see {@link PlatformDependentPlugin#initFilters()}.
 * 
 * @author Tchegito
 *
 */
@SuppressWarnings("unchecked")
public enum FilterEffect {
	FADE(FadeFilter.class, BilinearFilter.class),
	SEMIFADE(FadeFilter.class, BilinearFilter.class),
	BLEND(BlendFilter.class), 
	BLUR(BlurFilter.class), 
	ZOOM(ZoomFilter.class),
	CIRCLE(CircleFilter.class),
	CLOUD(CloudFilter.class);

	// RedFilter isn't declared here because it doesn't need
	// to be platform-specific. It's just a red box on whole screen.
	
	private Class<? extends ScreenFilter>[] clazz;
	
	public Class<? extends ScreenFilter>[] getFilterClass() {
		return clazz;
	}
	
	private FilterEffect(Class<? extends ScreenFilter>... p_clazz) {
		clazz = p_clazz;
	}
	
	public static FilterEffect fromInt(int p_int) {
		return values()[p_int];
	}
}
