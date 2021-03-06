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

package zildo.monde.sprites.persos.action;

import zildo.monde.sprites.persos.Perso;
import zildo.server.EngineZildo;

/**
 * @author Tchegito
 *
 */
public class ScriptedPersoAction implements PersoAction {

	Perso perso;
	String actionName;
	
	public ScriptedPersoAction(Perso p_perso, String p_actionName) {
		perso = p_perso;
		// Parse action name for arguments
		int posParenthese = p_actionName.indexOf('(');
		String[] argVals = null;
		if (posParenthese != -1) {
			actionName = p_actionName.substring(0, posParenthese);
			String argStr= p_actionName.substring(posParenthese+1, p_actionName.indexOf(')'));
			argVals = argStr.split(",");
		} else {
			actionName = p_actionName;
		}
		EngineZildo.scriptManagement.runPersoAction(perso, actionName, argVals);
	}
	
	@Override
	public boolean launchAction() {
		return perso.getAttente() == 0;
	}

}
