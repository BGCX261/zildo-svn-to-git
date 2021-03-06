package zeditor.windows.managers;

import java.io.File;

import javax.swing.JFileChooser;

import zeditor.windows.MasterFrame;
import zildo.resource.Constantes;

public class ExplorerFrameManager {
	public static final int SAVE = 0;
	public static final int OPEN = 1;

	private MasterFrame masterFrame;
	private JFileChooser fileChooser;
	private int mode;

	/**
	 * Constructeur vide
	 */
	public ExplorerFrameManager() {
	}

	/**
	 * Constructeur avec param�tres
	 * 
	 * @param p_frame
	 */
	public ExplorerFrameManager(MasterFrame p_frame, JFileChooser p_fileChooser) {
		masterFrame = p_frame;
		fileChooser = p_fileChooser;
	}

	/**
	 * Initialisation de certains champs en fonction du mode d'ouverture (OPEN ou SAVE)
	 * 
	 * @param p_mode
	 *            est le mode d'ouverture de la fen�tre
	 */
	public void init(int p_mode) {
		this.mode = p_mode;
		if (p_mode == SAVE) {
			masterFrame.setTitle("Enregistrer sous...");

		} else if (p_mode == OPEN) {
			masterFrame.setTitle("Ouvrir...");
		}
	}

	/**
	 * Ex�cute l'action en fonction du mode d'ouverture : sauvegarde ou ouverture
	 */
	public void doAction() {
		if (mode == SAVE) {
			save();
		} else if (mode == OPEN) {
			open();
		}
	}

	/**
	 * Ouvre la carte s�lectionn�e
	 */
	public void open() {
		File f = fileChooser.getSelectedFile();
		// On teste la nature du fichier (si c'est un *.MAP)
		if (!f.getName().endsWith(".map")) {
			// TODO Fen�tre d'erreur
			return;
		}
		String relativeFilename = f.getAbsolutePath().replace(Constantes.DATA_PATH + Constantes.MAP_PATH, "");

		masterFrame.getManager().loadMap(relativeFilename, null);

	}

	/**
	 * Sauvegarde la carte en cours dans le fichier s�lectionn�
	 */
	public void save() {
		if (fileChooser.getSelectedFile() == null) {
			return;
		}

		masterFrame.getManager().saveAs(fileChooser.getSelectedFile().getName());
	}
}
