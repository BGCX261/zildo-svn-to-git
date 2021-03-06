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

package zildo.fwk.script.xml.element.action.runtime;

import java.util.List;

import zildo.fwk.script.xml.element.LanguageElement;
import zildo.fwk.script.xml.element.QuestElement;
import zildo.fwk.script.xml.element.SceneElement;

/**
 * Runtime representation of a scene, because {@link SceneElement} is the model, thus unmodifiable.
 * @author Tchegito
 *
 */
public class RuntimeScene extends RuntimeModifiableElement {

	public final String id;
	public boolean locked;	// TODO: See if it could be final
	
	public final List<RuntimeAction> actions;
	public final SceneElement model;
	public boolean restoreZildo;
	
    
    // Marker to identify that a scene is created from an 'action' quest's tag
    public final static String MARQUER_SCENE = "@scene@";
    
	public RuntimeScene(List<LanguageElement> p_actions, QuestElement p_quest, boolean p_locked) {
		actions = createActions(p_actions);
		if (p_quest != null) {
			id = MARQUER_SCENE+p_quest.name;
			locked = p_quest.locked;
		} else {
			id = "fromActions";
			locked = p_locked;
		}
		model = null;
		restoreZildo = false;
	}
	
	public RuntimeScene(SceneElement scene, boolean p_locked) {
		id = scene.id;
		actions = createActions(scene.actions);
		locked = p_locked;
		model = scene;
		restoreZildo = scene.restoreZildo;
	}

	public RuntimeScene(List<RuntimeAction> p_actions, boolean p_locked) {
		actions = p_actions;
		id = "fromActions";
		locked = p_locked;
		model = null;
		restoreZildo = false;
	}
	
	@Override
	public String toString() {
		return actions.toString();
	}
}
