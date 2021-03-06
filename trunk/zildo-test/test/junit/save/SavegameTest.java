package junit.save;

import junit.framework.Assert;
import junit.perso.EngineUT;

import org.junit.Test;

import zildo.fwk.ZUtils;
import zildo.fwk.file.EasyBuffering;
import zildo.monde.Game;
import zildo.monde.sprites.desc.ZildoOutfit;
import zildo.server.EngineZildo;

public class SavegameTest extends EngineUT {

	@Test
	public void timeAfterReloadAndSave() {
		// 0) Create an hypothetic zildo
		EngineZildo.spawnClient(ZildoOutfit.Zildo);
		// 1) Create a game, and save
		Game game = new Game("preintro", "unitTest");
		EngineZildo.setGame(game);
		EasyBuffering buffer = new EasyBuffering(5000);
		game.serialize(buffer);
		int savedTime = EngineZildo.game.getTimeSpent();
		// 2) Reload this game
		game = Game.deserialize(buffer, false);
		EngineZildo.setGame(game);
		// 3) Wait some seconds
		ZUtils.sleep(4000);
		// 4) Save again
		game.serialize(buffer);
		// ==> check that time has been well updated
		int timeSpent = EngineZildo.game.getTimeSpent();
		Assert.assertTrue("Time measured at the end ("+timeSpent+") should have been greater than starting time ("+savedTime+")", 
				timeSpent > savedTime);
	}
	
	@Test
	public void timeAfterReloadAndWin() {
		// 0) Create an hypothetic zildo
		EngineZildo.spawnClient(ZildoOutfit.Zildo);
		// 1) Create a game, and save
		Game game = new Game("preintro", "unitTest");
		EngineZildo.setGame(game);
		EasyBuffering buffer = new EasyBuffering(5000);
		game.serialize(buffer);
		int savedTime = EngineZildo.game.getTimeSpent();
		// 2) Reload this game
		game = Game.deserialize(buffer, false);
		EngineZildo.setGame(game);
		// 3) Wait some seconds
		ZUtils.sleep(4000);
		// 4) Save again
		//game.serialize(buffer);
		// ==> check that time has been well updated
		int timeSpent = EngineZildo.game.getTimeSpent();
		Assert.assertTrue("Time measured at the end ("+timeSpent+") should have been greater than starting time ("+savedTime+")", 
				timeSpent > savedTime);
	}
}
