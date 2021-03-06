package zeditor.core.tiles;

import java.util.List;

import zeditor.core.prefetch.complex.DropDelegateDraw;
import zeditor.core.selection.CaseSelection;
import zeditor.windows.subpanels.SelectionKind;
import zildo.monde.map.Area;
import zildo.monde.map.Case;
import zildo.monde.map.Tile;
import zildo.monde.sprites.Reverse;
import zildo.monde.sprites.Rotation;
import zildo.monde.util.Point;

/**
 * Cette classe repr�sente une s�lection du TileSet. Elle est compos�e de :
 * <p>
 * <ul>
 * <li>La liste des �l�ments s�lectionn�s h�rit�e de la classe
 * {@link CaseSelection}</li>
 * <li>La largeur de la s�lection en nombre de cases</li>
 * <li>La hauteur de la s�lection en nombre de cases</li>
 * </ul>
 * </p>
 * <p>
 * Un �l�ment de la liste repr�sente l'id de la tuile. La liste <u>doit</u> �tre
 * remplie de cette mani�re :
 * </p>
 * <b>TileSet</b>
 * <table border="solid">
 * <tr>
 * <td></td>
 * <td>X</td>
 * <td>X+1</td>
 * <td>X+2</td>
 * </tr>
 * <tr>
 * <td>Y</td>
 * <td>A</td>
 * <td>B</td>
 * <td>C</td>
 * </tr>
 * <tr>
 * <td>Y+1</td>
 * <td>D</td>
 * <td>E</td>
 * <td>F</td>
 * </tr>
 * <tr>
 * <td>Y+2</td>
 * <td>G</td>
 * <td>H</td>
 * <td>I</td>
 * </tr>
 * </table>
 * <p>
 * <b>liste</b><br />
 * {A,B,C,D,E,F,G,H,I}
 * </p>
 * 
 * @author Drakulo
 * 
 */
public class TileSelection extends CaseSelection {
	/**
	 * Largeur de la s�lection en nombre de cases
	 */
	public int width;

	/**
	 * Hauteur de la s�lection en nombre de cases
	 */
	public int height;

	/**
	 * Constructeur vide
	 */
	public TileSelection() {
		super();
	}

	protected DropDelegateDraw drawer; // Class that handles the drawing of
										// TileSelection

	/**
	 * Constructeur
	 * 
	 * @param w
	 *            est la largeur de la s�lection (en nombre de cases)
	 * @param h
	 *            est la hauteur de la s�lection (en nombre de cases)
	 * @param l
	 *            est la liste contenant les �l�ments
	 */
	public TileSelection(Integer w, Integer h, List<Case> l) {
		super(l);
		width = w;
		height = h;

		// Default renderer for TileSelection. This could be overriden in
		// subclasses
		// (For example : PrefetchSelection)
		drawer = new DropDelegateDraw();
	}

	@Override
	public SelectionKind getKind() {
		return SelectionKind.TILES;
	}

	/**
	 * Surcharge de la m�thode toString afin de renvoyer une chaine contenant
	 * tous les items s�par�s par des virgules sans afficher les tiles non
	 * mapp�s qui ont �t� s�lectionn�s.
	 */
	@Override
	public String toString() {
		String s = null;
		boolean first = true;
		if (items != null && !items.isEmpty()) {
			s = "[" + width + "," + height + "] >> ";
			for (int i = 0; i < items.size(); i++) {
				if (first) {
					first = false;
				} else {
					s += ", ";
				}
				s += items.get(i);
			}
		}
		return s;
	}

	/**
	 * Draw a set of tiles on the given map at given location
	 * 
	 * @param map
	 * @param p
	 *            (map coordinates, basically 0..63, 0..63)
	 * @param p_mask
	 *            TRUE=user is in 'masked edit' mode
	 */
	public void draw(Area map, Point p, int p_mask) {
		int dx, dy;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				Case item = items.get(h * width + w);
				if (item != null) {
					dx = p.x + w;
					dy = p.y + h;
					if (map.getDim_x() > dx && map.getDim_y() > dy && dy >= 0
							&& dx >= 0) {
						// We know that this is a valid location
						Case c = map.get_mapcase(dx, dy + 4);
						if (c == null) {
							c = new Case();
							map.set_mapcase(dx, dy + 4, c);
						}

						// Calls the drawer to do the job
						drawer.draw(c, item, p_mask);
					}
				}
			}
		}
	}

	/**
	 * Modifiy 'reverse' attribute for a set of tile, at 'p' location, with the current size.
	 * @param map
	 * @param p
	 * @param p_mask
	 */
	public void reverse(Area map, Point p, int p_mask) {
		Reverse ref = null;
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					Case item =  map.get_mapcase(p.x/16 + w, p.y/16 + h + 4);
					if (item != null) {
						Tile tile;
						switch (p_mask) {
						case 0:
							tile = item.getBackTile();
							break;
						case 1:
							tile = item.getBackTile2();
							break;
						case 2:
							tile = item.getForeTile();
							break;
						default:
							throw new RuntimeException("Value "+p_mask+" is wrong for mask !");
						}
						if (tile != null) {
							if (tile.reverse == null) {
								tile.reverse = Reverse.NOTHING;
							}
							tile.reverse = tile.reverse.succ();
							if (ref == null) {
								ref = tile.reverse; // Keep a reference reverse value
							}
						}
						item.setModified(true);
					}
				}
			}
			// Reverse all tiles inside the zone
			if (width > 1 || height > 1) {
				if (ref == null) {
					ref = Reverse.NOTHING;
				}
				int revH=0, revW=0;
				Case[] keepCase = new Case[width * height];
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						switch (ref) {
						case ALL: case HORIZONTAL:
							revW = width - w - 1; revH = h; break;
						case VERTICAL:
							revW = w; revH = height - h - 1; break;
						case NOTHING:
							revW = width - w - 1; revH = height - h - 1;break;
						}
						keepCase[h * width + w] = map.get_mapcase(p.x/16 + revW, p.y/16 + revH + 4);
					}
				}
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						map.set_mapcase(p.x/16 + w, p.y/16 + h + 4, keepCase[h * width + w]);
					}
				}
			}
	}
	
	/**
	 * Modifiy 'rotate' attribute for a set of tile, at 'p' location, with the current size.
	 * @param map
	 * @param p
	 * @param p_mask
	 */
	public void rotate(Area map, Point p, int p_mask) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					Case item =  map.get_mapcase(p.x/16 + w, p.y/16 + h + 4);
					if (item != null) {
						Tile tile;
						switch (p_mask) {
						case 0:
							tile = item.getBackTile();
							break;
						case 1:
							tile = item.getBackTile2();
							break;
						case 2:
							tile = item.getForeTile();
							break;
						default:
							throw new RuntimeException("Value "+p_mask+" is wrong for mask !");
						}
						if (tile != null) {
							if (tile.rotation == null) {
								tile.rotation = Rotation.NOTHING;
							}
							tile.rotation = tile.rotation.succ();
						}
						item.setModified(true);
					}
				}
			}
			// Rotate all tiles inside the zone
			if ((width > 1 || height > 1) && width == height) {
				int rotH=0, rotW=0;
				Case[] keepCase = new Case[width * height];
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						rotW = h; rotH = width - w - 1;
						keepCase[h * width + w] = map.get_mapcase(p.x/16 + rotW, p.y/16 + rotH + 4);
					}
				}
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						map.set_mapcase(p.x/16 + w, p.y/16 + h + 4, keepCase[h * width + w]);
					}
				}
			}
	}
	
	@Override
	public List<Case> getElement() {
		return items;
	}

	public void finalizeDraw() {
	}
}
