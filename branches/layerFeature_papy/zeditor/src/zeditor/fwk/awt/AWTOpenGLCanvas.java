/* =============================================================================
 * AWTOpenGLCanvas.java Bubble_Engine 
 * com.evildevil.bubble.core
 * Copyright (c) 2004 - 2005 Benjamin "Evil-Devil" Behrendt
 * All rights reserved
 * -------------------------------------------------------------------------- */
package zeditor.fwk.awt;

import java.awt.GraphicsDevice;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.PixelFormat;

import zeditor.core.selection.ChainingPointSelection;
import zeditor.core.selection.Selection;
import zeditor.fwk.awt.ZildoCanvas.ZEditMode;
import zeditor.windows.managers.MasterFrameManager;
import zeditor.windows.subpanels.SelectionKind;
import zildo.Zildo;
import zildo.client.ClientEngineZildo;
import zildo.client.IRenderable;
import zildo.client.MapDisplay;
import zildo.fwk.gfx.Ortho;
import zildo.monde.map.Area;
import zildo.monde.map.ChainingPoint;
import zildo.monde.util.Angle;
import zildo.monde.util.Vector3f;
import zildo.monde.util.Vector4f;
import zildo.monde.util.Zone;
import zildo.platform.opengl.GLUtils;
import zildo.server.EngineZildo;

/**
 * @author Benjamin "Evil-Devil" Behrendt
 * @version 1.0, 28.12.2005
 */
public class AWTOpenGLCanvas extends AWTGLCanvas implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRenderable renderer = null;
	private Thread renderThread = null;
	private boolean initialize = false;

	protected ZildoScrollablePanel panel;

	MasterFrameManager manager;

	protected Point cursorLocation;
	protected Point cursorSize;
	protected Point startBlock; // For copy operation
	boolean zoom = false; // FALSE=editor view / TRUE=in-game view
	protected ZEditMode mode = ZEditMode.NORMAL;

	// Booleans for directional key pressed
	public boolean up;
	public boolean down;
	public boolean left;
	public boolean right;

	protected final static Point defaultCursorSize = new Point(16, 16);

	private final static Vector4f colChainingPoint = new Vector4f(0.8f, 0.7f, 0.8f, 1);
	private final static Vector4f colChainingPointSelected = new Vector4f(0.6f, 0.9f, 0.9f, 0.8f);
	private final static Vector4f colCursor = new Vector4f(1, 1, 1, 1);
	private final static Vector4f colCursorFore = new Vector4f(1, 1, 0, 1);
	private final static Vector3f colCursorText = new Vector3f(1, 0.2f, 0);

	// We have to communicate orders via boolean to this canvas
	// because we can use OpenGL outside of the paint process
	protected boolean changeMap = false;
	protected boolean changeSprites = false;

	private int sizeX;
	private int sizeY;

	int mask = 0; // 0=back / 1=back2 / 2=fore

	private boolean needToResize = false;
	private boolean needCapture = false;
	private boolean blockPaint = false;

	private float alpha;

	public AWTOpenGLCanvas() throws LWJGLException {
		super();
	}

	public AWTOpenGLCanvas(PixelFormat pixel_format) throws LWJGLException {
		super(pixel_format);
	}

	public AWTOpenGLCanvas(GraphicsDevice device, PixelFormat pixel_format) throws LWJGLException {
		super(device, pixel_format);
	}

	public AWTOpenGLCanvas(GraphicsDevice device, PixelFormat pixel_format, Drawable drawable) throws LWJGLException {
		super(device, pixel_format, drawable);
	}

	public AWTOpenGLCanvas(IRenderable renderable) throws LWJGLException {
		this();
		setRenderer(renderable);
	}

	public AWTOpenGLCanvas(PixelFormat pixelFormat, IRenderable renderable) throws LWJGLException {
		super(pixelFormat);
		setRenderer(renderable);
	}

	public AWTOpenGLCanvas(GraphicsDevice device, PixelFormat pixelFormat, IRenderable renderable) throws LWJGLException {
		super(device, pixelFormat);
		setRenderer(renderable);
	}

	public AWTOpenGLCanvas(GraphicsDevice device, PixelFormat pixelFormat, Drawable drawable, IRenderable renderable) throws LWJGLException {
		super(device, pixelFormat, drawable);
		setRenderer(renderable);
	}

	public void setRenderer(IRenderable renderable) {
		this.renderer = renderable;
	}

	public void setManager(MasterFrameManager p_manager) {
		manager = p_manager;
	}

	@Override
	public void paintGL() {
		if (blockPaint) {
			return;
		}
		if (changeMap) {
			Area map = EngineZildo.getMapManagement().getCurrentMap();
			ClientEngineZildo.getMapDisplay().setCurrentMap(map);
			changeMap = false;
			changeSprites = true;
		}
		if (changeSprites) {
			// And the sprites
			EngineZildo.spriteManagement.updateSprites(true);
			ClientEngineZildo.spriteDisplay.setEntities(EngineZildo.spriteManagement.getSpriteEntities(null));
			changeSprites = false;
		}
		if (!initialize) {
			initRenderThread();
			initOpenGL();
			// give the renderer a note that we have initialized our main render
			// stuff
			// and init the renderer itself
			// renderer.initRenderer();
			renderer.setInitialized(true);
			manager.init();
			initialize = true;
		}
		try {
			makeCurrent();
			Ortho ortho = ClientEngineZildo.ortho;
			if (needToResize) {
				if (ortho != null) {
					ortho.setSize(sizeX, sizeY, zoom);
					needToResize = false;
				}

			}

			if (needCapture) {
				blockPaint = true;
				captureEntireMap();
				MasterFrameManager.display("PNG file saved.", MasterFrameManager.MESSAGE_INFO);
				blockPaint = false;
				needCapture = false;
			} else {
				renderer.renderScene();
			}
			alpha += 0.04f;
			float sin = (float) Math.sin(alpha);

			// Draw chaining points
			Point shift = panel.getPosition();
			Area map = EngineZildo.getMapManagement().getCurrentMap();
			List<ChainingPoint> chaining = map.getChainingPoints();
			Selection sel = manager.getSelection();
			ChainingPoint selected = null;
			if (sel != null && sel.getKind() == SelectionKind.CHAININGPOINT) {
				selected = ((ChainingPointSelection) sel).getElement();
			}
			Vector4f col = colChainingPointSelected;
			col.w = sin;
			for (ChainingPoint ch : chaining) {
				Zone p = ch.getZone(map);
				int px1 = p.getX1();
				int px2 = p.getX2();
				int py1 = p.getY1();
				int py2 = p.getY2();
				ortho.boxv(px1 - shift.x, py1 - shift.y, px2, py2, 0, colChainingPoint);
				if (selected != null && selected == ch) {
					ortho.enableBlend();
					ortho.box(px1 - shift.x + 1, py1 - shift.y + 1, px2 - 2, py2 - 2, 0, col);
					ortho.disableBlend();
				}
				// Draw coming angle
				int centerX = (2 * px1 + px2) / 2;
				int centerY = (2 * py1 + py2) / 2;
				Angle coming = ch.getComingAngle();
				if (coming != Angle.NULL) {
					int arrowX = 1 + coming.coords.x * 32;
					int arrowY = 1 + coming.coords.y * 32;
					ortho.box(centerX - shift.x - 1, centerY - shift.y - 1, arrowX + 1, arrowY + 1, 0, col);
				} else {
					// Angle is null => chaining point is a landing position
					ortho.boxv(centerX - shift.x - 24, centerY - shift.y - 12, 48, 24, 0, col);
				}
			}

			// Draw rectangle
			if (cursorLocation != null) {
				Point start = cursorLocation;
				Point size = cursorSize;
				Point camera = panel.getCameraTranslation();
				if (cursorSize == null) {
					size = defaultCursorSize;
				}
				if (mode == ZEditMode.COPY_DRAG) {
					start = new Point(startBlock);
					start.translate(camera.x, camera.y);
					size = new Point(cursorLocation);
					size.x -= startBlock.x;
					size.y -= startBlock.y;
					size.translate(-camera.x, -camera.y);
				}
				ortho.boxv(start.x, start.y, size.x, size.y, 0, colCursor);
				if (mask > 0) {
					ortho.boxv(start.x + 2, start.y + 2, size.x - 4, size.y - 4, 0, colCursor);
					if (mask > 1) {
						ortho.boxv(start.x + 4, start.y + 4, size.x - 8, size.y - 8, 0, colCursorFore);
					}
				}
				switch (mode) {
				case TILE_REVERSE_EDIT:
					ortho.drawText(start.x + 2, start.y + 6, "Rev", colCursorText);
					break;
				case TILE_ROTATE_EDIT:
					ortho.drawText(start.x + 2, start.y + 6, "Rot", colCursorText);
					break;
				default:
					break;
				}
			}

			// Draw map limits
			int limitX = map.getDim_x() * 16 - shift.x;
			int limitY = map.getDim_y() * 16 - shift.y;
			ortho.box(limitX, 0, 1, sizeY, 0, colCursor);
			ortho.box(0, limitY, sizeX, 1, 0, colCursor);

			swapBuffers();
		} catch (LWJGLException lwjgle) {
			// should not happen
			lwjgle.printStackTrace();
		}
	}

	private final void initRenderThread() {
		if (renderThread == null) {
			renderThread = new Thread(this);
			renderThread.start();
		}
	}

	private void manageKeys() {
		if (left) {
			panel.horizontal.decrease();
		}
		if (right) {
			panel.horizontal.increase();
		}
		if (up) {
			panel.vertical.decrease();
		}
		if (down) {
			panel.vertical.increase();
		}
	}

	public void run() {
		while (renderThread != null && renderThread.isAlive()) {
			synchronized (this) {
				repaint();
				manageKeys();
				try {
					Thread.sleep(16);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}

	/**
	 * User ask a resize. We plan it to next rendering.
	 */
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		sizeX = width;
		sizeY = height;
		needToResize = true;
	}

	public void setZoom(boolean p_zoom) {
		if (zoom != p_zoom) {
			panel.setZoom(p_zoom);

			zoom = p_zoom;
			needToResize = true;
		}
	}

	public boolean isZoom() {
		return zoom;
	}

	/**
	 * If the context is created and a IRenderable object is provided, then
	 * IRenderable.initRenderer() will be called to setup the states etc that
	 * were used.
	 * 
	 * @exception throws LWJGLExeption if there is still no context available
	 * @exception throws NullPointerException if there was no IRenderable object
	 *            provided
	 */
	protected void initOpenGL() {
		if (getContext() == null)
			new LWJGLException("There is no context available that could be used to render something to it.");
		if (renderer == null)
			new NullPointerException("No IRenderable instance found, can't be null. Need one to render a scene.");
		renderer.initRenderer();
	}

	public IRenderable getRenderer() {
		return this.renderer;
	}

	@Override
	public void removeNotify() {
		if (renderThread != null && renderThread.isAlive()) {
			renderThread = null;
		}
		cleanUp();
		super.removeNotify();
	}

	public void cleanUp() {

	}

	private void captureEntireMap() throws LWJGLException {
		Ortho ortho = ClientEngineZildo.ortho;
		Area area = EngineZildo.getMapManagement().getCurrentMap();
		MapDisplay	mapDisplay = ClientEngineZildo.getMapDisplay();
		int totalWidth = area.getDim_x() * 16;
		int totalHeight = area.getDim_y() * 16;
		ortho.setSize(sizeX, sizeY, false);
		// Save the rendered scene
		ByteBuffer bigOne = BufferUtils.createByteBuffer(totalWidth * totalHeight * 3);
		int width = Zildo.screenX;
		int height = Zildo.screenY;
		int y = 0;
		int pas;
		int addY = 0;
		if (totalHeight < height) {
			addY = height - totalHeight;
		}
		zildo.monde.util.Point camera = new zildo.monde.util.Point(mapDisplay.getCamera());
		while (true) { // y
			int x = 0;
			while (true) { // X
				mapDisplay.setCamera(new zildo.monde.util.Point(x, y));
				makeCurrent();
				renderer.renderScene();
				swapBuffers();
				ByteBuffer temp = ClientEngineZildo.openGLGestion.capture();
				int copyWidth = Math.min(width, totalWidth);
				int copyHeight = Math.min(height, totalHeight);
				GLUtils.copy(temp, bigOne, copyWidth, copyHeight, totalWidth, x, y, addY, false);
				pas = Math.min(width, totalWidth - x - width);
				if (pas <= 0) {
					break;
				}
				x += pas;
			}
			pas = Math.min(height, totalHeight - y - height);
			if (pas <= 0) {
				break;
			}
			y += pas;
		}
		String name = area.getName().replace(".map", "");
		GLUtils.saveBufferAsPNG("c:\\kikoo\\" + name, bigOne, totalWidth, totalHeight, false);
		// Reset camera and zoom
		mapDisplay.setCamera(camera);
		ortho.setSize(sizeX, sizeY, zoom);
	}

	/**
	 * @return the mode
	 */
	public ZEditMode getMode() {
		return mode;
	}

	/**
	 * @param p_mode
	 *            the mode to set
	 */
	public void setMode(ZEditMode p_mode) {
		mode = p_mode;
	}

	public void setChangeSprites(boolean p_value) {
		changeSprites = p_value;
	}

	public void askCapture() {
		needCapture = true;
	}
}