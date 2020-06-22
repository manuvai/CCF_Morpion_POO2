
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * @author mrehua
 *
 */

public class Main extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	String title = "Morpion";
	int width = 300,
		height = 300;
		
	GamePanel p_mainContainer = new GamePanel();
	
	public Main() {
		setTitle(title);
		setSize(width, height);
		setLocationRelativeTo(null);
		setResizable(false);
		
		init();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public static void main(String[] arg) {
		new Main();
	}
	
	/**
	 * Initialize the panel of the frame.
	 * 
	 */
	
	public void init() {
		setLayout(new BorderLayout());
		add(p_mainContainer, BorderLayout.CENTER);
	}

}
