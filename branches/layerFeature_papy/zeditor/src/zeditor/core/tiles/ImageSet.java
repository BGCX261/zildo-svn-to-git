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

package zeditor.core.tiles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import zeditor.core.selection.Selection;
import zeditor.tools.Transparency;
import zeditor.windows.managers.MasterFrameManager;
import zildo.fwk.ZUtils;
import zildo.monde.util.Zone;

/**
 * Abstract class handling image containing arbitrary sized sections.
 * 
 * @author Tchegito
 * 
 */
@SuppressWarnings("serial")
public abstract class ImageSet extends JPanel {

	protected String tileName;
	protected Image currentTile;
	protected Integer tileWidth;
	protected Integer tileHeight;
	protected Map<String, Image> tiles;

	// Selection
	protected Point startPoint;
	protected Point stopPoint;
	protected Selection currentSelection;
	protected List<Zone> selectables;

	protected MasterFrameManager manager;

	/**
	 * Constructeur avec param�tres
	 * 
	 * @param p_tileName
	 *            : Nom du set de tuiles en cours
	 * @author tchegito
	 */
	public ImageSet(String p_tileName, MasterFrameManager p_manager) {
		tiles = new HashMap<String, Image>();

		manager = p_manager;

		// D�finition du layout afin d'afficher le Tile en haut du conteneur
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		tileName = p_tileName;

		selectables = new ArrayList<Zone>();

		this.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				if (currentTile != null) {
					if (MouseEvent.BUTTON1 == e.getButton()) {
						// Reinitialize points for new selection
						startPoint = null;
						stopPoint = null;

						Zone z = getObjectOnClick(e.getX(), e.getY());
						if (z != null) {
							int x = z.getX1();
							int y = z.getY1();
							if (startPoint == null) {
								startPoint = new Point(x, y);
							} else {
								startPoint.setLocation(x, y);
							}
							repaint();
						}
					} else if (MouseEvent.BUTTON3 == e.getButton()) {
						// right click
						if (currentSelection != null) {
							handleSelectionRightClick(e);
						}
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent e) {
				if (currentTile != null) {
					if (MouseEvent.BUTTON1 == e.getButton()) {
						// Click gauche
						Zone z = getObjectOnClick(e.getX(), e.getY());
						if (z != null) {
							int x = z.getX1() + z.getX2();
							int y = z.getY1() + z.getY2();
							if (stopPoint == null) {
								stopPoint = new Point(x, y);
							} else {
								stopPoint.setLocation(x, y);
							}

							if (startPoint != null) {
								// On trie les points si le startPoint est
								// valide
								sortPoints();
								// On construit la nouvelle s�lection
								buildSelection();
							}
							repaint();
						}

					} else if (MouseEvent.BUTTON3 == e.getButton()) {
						// Click droit

					}
				}
			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				if (currentTile != null) {
					Zone z = getObjectOnClick(e.getX(), e.getY());
					if (z != null) {
						int x = z.getX1() + z.getX2();
						int y = z.getY1() + z.getY2();
						if (stopPoint == null) {
							stopPoint = new Point(x, y);
						} else {
							stopPoint.setLocation(x, y);
						}
						// Repaint du Tile
						repaint();
					}
				}
			}

			public void mouseMoved(MouseEvent arg0) {
			}
		});
	}

	protected Zone getObjectOnClick(int p_x, int p_y) {
		for (Zone z : selectables) {
			if (z.isInto(p_x, p_y)) {
				return z;
			}
		}
		// No object under the mouse
		return null;
	}

	/**
	 * M�thode priv�e de tri des Points de s�lection. Le point en haut � gauche
	 * devient le point de d�part et le point en bas � droite devient le point
	 * de fin.
	 */
	private void sortPoints() {
		// A partir des points de d�but et de fin, on recr�e deux nouveaux
		// points afin d'avoir le point de d�but en haut � gauche et le point
		// de fin en bas � droite
		int x1 = Math.min(startPoint.x, stopPoint.x);
		int y1 = Math.min(startPoint.y, stopPoint.y);
		int x2 = Math.max(startPoint.x, stopPoint.x);
		int y2 = Math.max(startPoint.y, stopPoint.y);
		startPoint.setLocation(x1, y1);
		stopPoint.setLocation(x2, y2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		// Redimensionnement de la taille du TileSet pour le Scroll automatique
		g.clearRect(0, 0, getWidth(), getHeight());
		if (currentTile != null) {
			setPreferredSize(new Dimension(currentTile.getWidth(null),
					currentTile.getHeight(null)));
		} else {
			setPreferredSize(new Dimension(0, 0));
		}
		revalidate();

		// Repaint
		Graphics2D g2d = (Graphics2D) g;
		while (!g2d.drawImage(currentTile, 0, 0,
				Transparency.BANK_TRANSPARENCY, null)) {
			// If image isn't ready yet, wait then retry
			ZUtils.sleep(100);
		}

		// On dessine un cadre autour des tuiles s�lectionn�es en dernier pour
		// qu'il soit au dessus
		if (stopPoint == null) {
			// Le point stopDrag est null donc on met le cadre sur une simple
			// case
			drawRectangle(g2d, Color.black, Color.white, startPoint, startPoint);
		} else {
			// Le point de stopDrag n'est pas null donc on doit tracer un
			// rectangle sur plusieurs cases
			drawRectangle(g2d, Color.black, Color.white, startPoint, stopPoint);
		}

		specificPaint(g2d);

		g2d.dispose();

	}

	/**
	 * * M�thode de trac� du cadre autour des tuiles s�lectionn�es
	 * 
	 * @param g
	 *            : Graphics sur lequel on va dessiner
	 * @param outer
	 *            : Couleur de l'ext�rieur de cadre
	 * @param inner
	 *            : Couleur de l'int�rieur de cadre
	 * @param p_startPoint
	 *            : Point de d�part du cadre
	 * @param p_stopPoint
	 *            : Point de fin du cadre
	 * @author Drakulo
	 */
	protected void drawRectangle(Graphics g, Color outer, Color inner,
			Point p_startPoint, Point p_stopPoint) {
		if (p_startPoint != null && p_stopPoint != null) {
			int xDep, yDep, xFin, yFin;
			xDep = Math.min(p_startPoint.x, p_stopPoint.x);
			xFin = Math.max(p_startPoint.x, p_stopPoint.x);
			yDep = Math.min(p_startPoint.y, p_stopPoint.y);
			yFin = Math.max(p_startPoint.y, p_stopPoint.y);

			g.setColor(outer);
			g.drawRect(xDep, yDep, xFin - xDep, yFin - yDep);
			g.setColor(inner);
			g.drawRect(xDep + 1, yDep + 1, xFin - xDep - 2, yFin - yDep - 2);
			g.setColor(outer);
			g.drawRect(xDep + 2, yDep + 2, xFin - xDep - 4, yFin - yDep - 4);
		}
	}

	/**
	 * Abstract method to override depending on the kind of selection we want.
	 */
	protected abstract void buildSelection();

	protected abstract void specificPaint(Graphics2D p_g2d);

	protected void handleSelectionRightClick(MouseEvent e) {
	};
}
