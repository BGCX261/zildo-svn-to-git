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

package zildo.client.gui.menu;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import zildo.Zildo;
import zildo.client.stage.SinglePlayer;
import zildo.fwk.file.EasyBuffering;
import zildo.fwk.file.EasyWritingFile;
import zildo.fwk.ui.InfoMenu;
import zildo.fwk.ui.ItemMenu;
import zildo.fwk.ui.Menu;
import zildo.fwk.ui.PageableMenu;
import zildo.fwk.ui.UIText;
import zildo.fwk.ui.UnselectableItemMenu;
import zildo.monde.Game;
import zildo.monde.sprites.persos.PersoZildo;
import zildo.resource.Constantes;
import zildo.server.EngineZildo;

/**
 * @author Tchegito
 * 
 */
public class SaveGameMenu extends PageableMenu {

	final boolean load;

	/**
	 * Constructor for save game menu.
	 * 
	 * @param p_savegames
	 * @param p_load
	 *            TRUE=client want to load / FALSE=save
	 * @param p_previous
	 */
	public SaveGameMenu(boolean p_load, Menu p_previous) {
		super("m8.title");

		previousMenu = p_previous;
		load = p_load;

		final List<String> savegames = SaveGameMenu.findSavegame();
		items = new ArrayList<ItemMenu>();
		if (p_load && savegames.isEmpty()) {
			items.add(new UnselectableItemMenu("m8.info.nosave") { });
			setTitle("");
		} else {
			for (final String s : savegames) {
				items.add(new ItemMenu(s) {
					@Override
					public void run() {
						int number = getSavegameNumber(s);
						String filename = getSavegameFilename(number);
						if (!load) {
							saveGame(filename);
							client.handleMenu(new InfoMenu("m8.info.ok",
									previousMenu));
						} else {
							if (!loadGame(filename, false)) {
	                            if (!loadGame(filename, true)) {
	                            	// Load failed !
	                                client.handleMenu(new InfoMenu("m8.info.nok", currentMenu));
	                            } else {
	                            	// Game has been loaded in legacy (previous version) without name
	                            	// ==> can't display message for now !
	                            }
	                        }
						}
					}
				});
			}
		}

		if (!p_load) {
			items.add(new ItemMenu("m8.create") {
				@Override
				public void run() {
					// Save the game
					int i = 1;
					int temp;
					while (true) { // Find a filename which doesn't exist yet
						temp = i++;
						boolean found = false;
						for (String name : savegames) {
							int number = getSavegameNumber(name);
							if (!found && number == temp) {
								found = true;
							}
						}
						if (!found) {
							break;
						}
					}
					String filename = Constantes.SAVEGAME_DIR
							+ Constantes.SAVEGAME_FILE + temp;
					saveGame(filename);
					client.handleMenu(new InfoMenu("m8.info.ok", previousMenu));
				}
			});
		}

		// Back button is handled by the PageableMenu
		setMenu(items.toArray(new ItemMenu[] {}));
	}

	/**
	 * Save the game
	 */
	private void saveGame(String p_filename) {
		EasyBuffering buffer = new EasyBuffering();
		EngineZildo.game.serialize(buffer);
		EasyWritingFile file = new EasyWritingFile(buffer);
		file.saveFile(p_filename);
	}

	private boolean loadGame(String p_filename, boolean p_legacy) {
		EasyBuffering file=Zildo.pdPlugin.openPrivateFile(p_filename);

		return loadGameFromBuffer(file, p_legacy);
	}

	public static boolean loadGameFromBuffer(EasyBuffering file, boolean p_legacy) {
		// Create a dummy game object, just to initialize server
		Game game = new Game(null, false);
		game.brandNew = false;
		SinglePlayer singlePlay = new SinglePlayer(game);

		// Compute global variables
		while (EngineZildo.scriptManagement.isScripting()) {
			EngineZildo.scriptManagement.render();
		}
		
		game = Game.deserialize(file, p_legacy);
		if (game == null) {
			// Problem occured while loading game
			return false;
		}
		UIText.setCharacterName(game.heroName);
		EngineZildo.setGame(game);
		if (game.mapName == null) {	// For backward compatibility
			game.mapName = "foretg2";
		}
		
		
		//game.mapName = "voleurs";
		//EngineZildo.persoManagement.getZildo().x+=50;
		
		
		// Wait for all history script to be finished
		// This could be dangerous : if a script can't finish => end of the story
		// (though it could happen, as soon as a script is broken ...)
		while (EngineZildo.scriptManagement.isScripting()) {
			EngineZildo.scriptManagement.render();
		}
		
		EngineZildo.getMapManagement().loadMap(game.mapName, false);

		//Area area = EngineZildo.mapManagement.getCurrentMap();
		/*
		Case c = area.get_mapcase(28, 47+4);
		c.setTransition(Angle.EST);
		c = area.get_mapcase(43, 47+4);
		c.setTransition(Angle.OUEST);
		*//*
		Case c = area.get_mapcase(29, 25+4);
		c.setTransition(Angle.NORD);
		c = area.get_mapcase(30, 25+4);
		c.setTransition(Angle.NORD);

		c = area.get_mapcase(29, 17+4);
		c.setTransition(Angle.SUD);
		c = area.get_mapcase(30, 17+4);
		c.setTransition(Angle.SUD);
		*/
		singlePlay.launchGame();
		
		// Clear the way around Zildo (open door for example)
		PersoZildo zildo = EngineZildo.persoManagement.getZildo();
		if (zildo != null) {
			zildo.walkTile(false);
		}
		return true;	// success
	}

	/**
	 * Find all savegames in current directory.
	 * 
	 * @return List<String>
	 */
	public static List<String> findSavegame() {
		File[] savegames = Zildo.pdPlugin.listFiles(Constantes.SAVEGAME_DIR, new SaveGameFilter());
		List<String> filenames = new ArrayList<String>();
		if (savegames != null && savegames.length > 0) { // Is there any savegames ?
			for (File f : savegames) {
				filenames.add(getSavegameDisplayTitle(f));
			}
		}
		// Sort filenames by savegame number
		Collections.sort(filenames, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				Integer i1 = Integer.valueOf(s1.substring(0, s1.indexOf(" ")));
				Integer i2 = Integer.valueOf(s2.substring(0, s2.indexOf(" ")));
				return i1.compareTo(i2);
			}
		});
		return filenames;
	}

	/**
	 * Simple filter to get the game files.
	 */
	public static class SaveGameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith(Constantes.SAVEGAME_FILE);
		}
	}

	/**
	 * Get the display for a savegame in File form.
	 * 
	 * @param p_file
	 * @return
	 */
	public static String getSavegameDisplayTitle(File p_file) {
		String name = p_file.getName().replace(Constantes.SAVEGAME_FILE, "");
		name += " "
				+ new SimpleDateFormat("dd.MM.yyyy HH-mm").format(new Date(
						p_file.lastModified()));
		return name;
	}

	public static int getSavegameNumber(String p_displayTitle) {
		return Integer.valueOf(p_displayTitle.split(" ")[0]);
	}

	public static String getSavegameFilename(int p_number) {
		return Constantes.SAVEGAME_DIR + Constantes.SAVEGAME_FILE + p_number;
	}
}
