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

package zildo.server.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zildo.fwk.script.command.ScriptExecutor;
import zildo.fwk.script.logic.IEvaluationContext;
import zildo.fwk.script.logic.SpriteEntityContext;
import zildo.fwk.script.xml.ScriptReader;
import zildo.fwk.script.xml.element.AdventureElement;
import zildo.fwk.script.xml.element.ConditionElement;
import zildo.fwk.script.xml.element.LanguageElement;
import zildo.fwk.script.xml.element.MapscriptElement;
import zildo.fwk.script.xml.element.PersoActionElement;
import zildo.fwk.script.xml.element.QuestElement;
import zildo.fwk.script.xml.element.SceneElement;
import zildo.fwk.script.xml.element.TriggerElement;
import zildo.monde.items.ItemKind;
import zildo.monde.map.ChainingPoint;
import zildo.monde.quest.StringReplacement;
import zildo.monde.quest.actions.ScriptAction;
import zildo.monde.sprites.desc.ElementDescription;
import zildo.monde.sprites.persos.Perso;
import zildo.monde.sprites.persos.PersoZildo;
import zildo.monde.util.Point;
import zildo.server.EngineZildo;
import zildo.server.Server;

 /** 
 * It provides two basic functions :<ul>
 * <li>render script (via {@link ScriptExecutor})</li>
 * <li>trigger an event</li>
 * </ul>
 * and more high-level functions :<ul>
 * <li>remember opened chest</li>
 * <li>remember opened doors</li>
 * </ul>
 * 
 * @author Tchegito
 *
 */
public class ScriptManagement {

    ScriptExecutor scriptExecutor;
    AdventureElement adventure=null;
    StringReplacement areaReplaces;
    StringReplacement zikReplaces;
    StringReplacement nameReplaces;
    
    final Map<String, QuestElement> questsByName;
    
    final Map<String, String> variables;
    
    // 'LOCATION' trigs for specific location on the current map
    final List<TriggerElement> locationTriggerOnMap;	
    
    // Marker to identify that a scene is created from an 'action' quest's tag
    public final static String MARQUER_SCENE = "@scene@";

    /**
     * Build a script management object.<br/>
     * Note that each time player loads/restarts a game, a new instance of this object is created.<br/>
     * So we have not to reinitialize all quest 'done' states, because previous object is garbage collected.
     */
    public ScriptManagement() {
        // Load adventure
        adventure=(AdventureElement) ScriptReader.loadScript("common", "people", "episode1", "episode2");

        scriptExecutor=new ScriptExecutor();
        
        areaReplaces=new StringReplacement();
        zikReplaces=new StringReplacement();
        nameReplaces = new StringReplacement();
        
        questsByName = new HashMap<String, QuestElement>();
        for (QuestElement quest : adventure.getQuests()) {
        	questsByName.put(quest.name, quest);
        }
        
        variables = new HashMap<String, String>();
        
    	locationTriggerOnMap = new ArrayList<TriggerElement>();
    	
    	execute("setupPeople", true);
    }
    
    public void render() {
    	scriptExecutor.render();
    }
    
    public boolean isScripting() {
    	return scriptExecutor.isScripting();
    }
    
    public void userEndAction() {
    	scriptExecutor.userEndAction();
    }
    
    /**
     * Execute the given named script, if it exists, and it's not already running.
     * @param p_name
     * @param p_locked TRUE=default:blocking scene / FALSE=non blocking scene
     */
    public void execute(String p_name, boolean p_locked) {
    	SceneElement scene=adventure.getSceneNamed(p_name);
    	if (scene != null) {
    		 if (!scriptExecutor.isProcessing(p_name)) {
    			 scene.locked = p_locked;
    			 scriptExecutor.execute(scene, true, false, null);
    		 }
    	} else {
    		throw new RuntimeException("Scene "+p_name+" doesn't exist !");
    	}
    }

    public void execute(List<LanguageElement> p_actions, boolean p_finalEvent, QuestElement p_quest, boolean p_topPriority, IEvaluationContext p_context) {
    	// Create a SceneElement from the given actions
		SceneElement scene=SceneElement.createScene(p_actions);
		if (p_quest != null) {
			scene.id = MARQUER_SCENE+p_quest.name;
			scene.locked = p_quest.locked;
		} else if (p_context != null) {
			scene.locked = false;	// Unlock when scene is contextual (may be tuned later)
		}
		// And execute this list
		scriptExecutor.execute(scene, p_finalEvent, p_topPriority, p_context);
    }
    
    /**
     * Entry point for all identifiable action, which could target a trigger.<p/>
     * Trigger could be any kind of QuestEvent.
     * @param p_triggerElement element created by a static method from TriggerElement
     */
    public void trigger(TriggerElement p_triggerElement) {
    	// 0: optimization, check LOCATION trigger on a restricted list
     	if (p_triggerElement.isLocationSpecific()) {
    		boolean atLeastOne=false;
    		for (TriggerElement trig : locationTriggerOnMap) {
    			if (!trig.done && trig.match(p_triggerElement)) {
    				trig.done=true;
    				atLeastOne=true;
    			}
    		}
    		if (!atLeastOne) {	// No one ? So get out !
    			return;
    		}
    	}
    	
    	// 1: check the existing triggers to potentially enable them
    	for (QuestElement quest : adventure.getQuests()) {
    		if (!quest.done) {
    			// For each quest undone yet :
    			for (TriggerElement trig : quest.getTriggers()) {
    				if (!trig.done && trig.match(p_triggerElement)) {
    					trig.done = true;
    				}
    			}
    		}
    	}
    	// 2: recheck all triggers to potentially accomplish a quest
    	for (QuestElement quest : adventure.getQuests()) {
    		if (!quest.done) {
    			// For each quest undone yet :
    			boolean achieved = quest.getTriggers().size() != 0;
    			for (TriggerElement trig : quest.getTriggers()) {
    				achieved&=trig.isDone();
    			}
    			// Particular case : no triggers at all IS NOT considered as achieved
    			if (achieved) {
    				accomplishQuest(quest, true);
    			} else if (quest.isTriggersBoth()) {
    				// All trigger are not activated at the same time ==> then we reset them to 'undone'
    				for (TriggerElement trig : quest.getTriggers()) {
    					trig.done=false;
    				}
    			} else {
    				// Reset only the 'location' and 'dialog' trigger to 'undone' 
    				// (because they have to be immediate)
    				for (TriggerElement trig : quest.getTriggers()) {
    					if (trig.done && trig.isImmediate()){
    						switch (trig.kind) {
    						case LOCATION:
	    						TriggerElement currentMapTrigger = EngineZildo.mapManagement.getCurrentMapTrigger();
	    						if (trig.isLocationSpecific() || !trig.match(currentMapTrigger)) {
	    							trig.done = false;
	    						}
	    						break;
    						case DIALOG:
    							trig.done = false;
    							break;
    						}
    					}
    				}    				
    			}
    		}
    	}
    }

    /**
     * Method designed for optimization : we have to chek at each character's movement, if he activates
     * a trigger. So, this must be a fast operation.
     * @param p_mapName
     */
    public void prepareMapSubTriggers(String p_mapName) {
    	locationTriggerOnMap.clear();
    	for (QuestElement quest : adventure.getQuests()) {
    		if (!quest.done) {
    			// For each quest undone yet :
    			for (TriggerElement trig : quest.getTriggers()) {
    				if (trig.isLocationSpecific() && p_mapName.equals(trig.getName())) {
    					locationTriggerOnMap.add(trig);
    				}
    			}
    		}
    	}
    }

    /**
     * Update quest status, and launch the associated actions.
     * @param p_questName
     * @param p_trigger
     */
    public void accomplishQuest(String p_questName, boolean p_trigger) {
    	boolean found=false;
    	QuestElement quest = questsByName.get(p_questName);
    	if (quest != null) {
   			accomplishQuest(quest, p_trigger);
    	} else {
	    	if (!found) { // Given quest hasn't been found, so create it (useful for automatic behaviors like chest and doors)
	    		quest=new QuestElement();
	    		quest.name=p_questName;
	    		quest.done=true;	// Set it done
	    		questsByName.put(p_questName, quest);
	    		adventure.addQuest(quest);
	    	}
    	}
    }
    
    /**
     * Update quest status, and launch the associated actions.
     * @param p_quest
     * @param p_trigger TRUE=we have to launch targeted action / FALSE=just set quest to 'done' state
     */
    private void accomplishQuest(QuestElement p_quest, boolean p_trigger) {
    	//System.out.println("Accomplish "+p_quest.name);
   		p_quest.done=true;
    	

		
		// 1) execute the immediate actions (only in-game)
    	if (p_trigger) {
	    	// Target potentials triggers
	    	TriggerElement trig=TriggerElement.createQuestDoneTrigger(p_quest.name);
	    	trigger(trig);
			// Execute the corresponding actions
			execute(p_quest.getActions(), true, p_quest, false, null);
    	}

    	// 2) note the history events (mapReplace ...)
    	// It will be executed AFTER the entire script in 'actions' tag
    	List<LanguageElement> history=p_quest.getHistory();
		if (history != null) {
			execute(history, true, null, false, null);
		}
    	
    }
    
    /**
     * Declare a quest "undone". (used for quests with attribute "repeat")
     * @param p_questName
     */
    public void resetQuest(String p_questName) {
    	QuestElement quest = questsByName.get(p_questName);
    	if( quest != null) {
    		quest.done = false;
    		for (TriggerElement trigger : quest.getTriggers()) {
    			trigger.reset();
    		}
    	}
    }
    
    public String getReplacedMapName(String p_mapName) {
        return areaReplaces.getValue(p_mapName);
    }
    
    public String getReplacedZikName(String p_zikName) {
        return zikReplaces.getValue(p_zikName);
    }
    
    public String getReplacedPersoName(String p_persoName) {
    	return nameReplaces.getValue(p_persoName);
    }
    
    public void addReplacedMapName(String p_ancient, String p_new) {
    	areaReplaces.put(p_ancient, p_new);
    }

    public void addReplacedZikName(String p_ancient, String p_new) {
    	zikReplaces.put(p_ancient, p_new);
    }
    
    public void addReplacedPersoName(String p_ancient, String p_new) {
    	String[] names = p_ancient.split(",");
    	for (String s : names) {
    		nameReplaces.put(s, p_new);
    	}
    }
    
	public AdventureElement getAdventure() {
		return adventure;
	}
		
	/**
	 * Executes some automatic script when Zildo picks an item up.
	 * @param p_zildo
	 * @param p_kind
	 */
	public void automaticBehavior(PersoZildo p_zildo, ItemKind p_kind, ElementDescription p_desc) {
		if (isScripting()) {
			return;	// Go away, because a script has already taken the lead
		}
		String label;
		if (p_kind == null) {
			String add="";
	    	if (p_desc == ElementDescription.HEART_FRAGMENT || p_desc == ElementDescription.HEART_FRAGMENT2) {
	    		// Specific for moon fragment
	    		if (!p_zildo.hasItem(ItemKind.NECKLACE)) {
	    			add="0";
	    		} else if (p_zildo.getMoonHalf() >= 2) {
	    			add="3";
	    		} else if (p_zildo.getMoonHalf() == 1) {
	    			add="2";
	    		} 
	    	}
			label = p_desc.getFoundSentence(add);
		} else {
	        switch (p_kind) {
	        case BOW:
	        	p_zildo.setCountArrow(p_zildo.getCountArrow() + 5);	// Bow comes with 5 arrows
	        case BOMB:
	        	p_zildo.setCountBomb(p_zildo.getCountBomb() + 5);	// 5 bombs at start
	        default:
	        	break;
	        }
	    	label=p_kind.getFoundSentence();
		}
    	if (label != null) {
    		ClientState client = Server.getClientFromZildo(p_zildo);
    		EngineZildo.dialogManagement.launchDialog(client, null, new ScriptAction(label));
    	}
	}
	
	public void runPersoAction(Perso perso, String name) {
		PersoActionElement action = adventure.getPersoActionNamed(name);
		if (action != null) {
			SpriteEntityContext context = new SpriteEntityContext(perso);
			execute(action.actions, true, null, false, context);
			perso.setAttente(action.duration);
		}
	}
	
	/**
	 * Clear all scripts that are non blocking (example: PersoAction, or moving platforms)
	 */
	public void clearUnlockingScripts() {
		scriptExecutor.clearUnlockingScripts();
	}
	/**
	 * Returns TRUE if the given quest is marked as done.
	 * @param p_questName
	 * @return boolean
	 */
	public boolean isQuestDone(String p_questName) {
		QuestElement quest = questsByName.get(p_questName);
		if (quest == null) {
			return false;
		}
		return quest.done;
	}
	
	/**
	 * Returns TRUE if the given quest is done and its actions are achieved.
	 * @param p_questName
	 * @return boolean
	 */
	public boolean isQuestOver(String p_questName) {
		boolean done = isQuestDone(p_questName);
		if (done && isScripting()) {
			// Check if this quest is currently active
			return !scriptExecutor.isProcessing(p_questName);
		}
		return done;
	}
	
	public boolean isQuestProcessing(String p_questName) {
		return scriptExecutor.isProcessing(p_questName);
	}
	
	public boolean isOpenedChest(String p_mapName, Point p_location) {
		String questName=p_mapName+p_location.toString();
		return isQuestDone(questName);
	}
	
	public void openChest(String p_mapName, Point p_location) {
		String questName=p_mapName+p_location.toString();
		accomplishQuest(questName, true);
	}
	
	/**
	 * Build a quest's keyname about a chaining point between 2 maps.
	 * @param p_mapName
	 * @param p_ch
	 * @return String
	 */
	private String buildKeyDoor(String p_mapName, ChainingPoint p_ch) {
		String map2=p_ch.getMapname();
		String key=p_mapName.compareTo(map2) < 0 ? p_mapName+map2 : map2+p_mapName;
		key+=p_ch.getOrderX()+p_ch.getOrderY();

		return key;
	}
	
	public boolean isOpenedDoor(String p_mapName, ChainingPoint p_ch) {
		return isQuestDone(buildKeyDoor(p_mapName, p_ch));
	}
	
	public void openDoor(String p_mapName, ChainingPoint p_ch) {
		accomplishQuest(buildKeyDoor(p_mapName, p_ch), false);
	}
	
	public void doMapReplacements(String p_mapName) {
		for (MapscriptElement mapScript : adventure.getMapScripts()) {
			for (ConditionElement condi : mapScript.getConditions()) {
				if (condi.mapName.equals(p_mapName) && condi.isRight()) {
					// Execute the 'mapscript' before all, with topPriority=TRUE
					execute(condi.getActions(), false, null, true, null);
				}
			}
		}
	}
	
	/**
	 * Returns TRUE if we can propose blue drop to the player.<br/>
	 * It's allowed only when player got the necklace.<br/>
	 * @return boolean
	 */
	public boolean isBlueDropDisplayable() {
		PersoZildo zildo = EngineZildo.persoManagement.getZildo();
		return (zildo != null && zildo.hasItem(ItemKind.NECKLACE));
	}
	
	public Map<String, String> getVariables() {
		return variables;
	}
	
	public String getVarValue(String p_varName) {
		if (variables == null) {
			return null;
		}
		return variables.get(p_varName);
	}
}