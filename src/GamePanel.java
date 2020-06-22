import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
	
	static final String CROIX_SIGNE = "X",
						ROND_SIGNE = "O";
	
	String coche = CROIX_SIGNE;
	
	volatile boolean running = false;
	
	private Image dbImage;
	private Graphics dbg;
	
	Thread game;
	
	private long period = 6 * 1000000;		//ms -> nano // sleeping time
	private static final int DELAYS_BEFORE_YIELD = 10;
	static final int GWIDTH = 300, GHEIGHT = 300;
	
	MouseListener mouseListener = new MouseListener();

	int[] score = {0, 0};
	
	Case[] tab_case = new Case[9];
	
	public GamePanel()  {
		for (int i = 0; i < tab_case.length; i++) {
			tab_case[i] = new Case(i);
		}
		
		addMouseListener(mouseListener);
	}
	
	void setCoche(String coche, int position) {
		tab_case[position].setCoche(coche);
	}
	
	int verifSuite() {
		
		int pos = -1;
		
		for (int i = 0; i < 3; i++) {
			if (tab_case[i].coche == tab_case[i + 1].coche && 
				tab_case[i].coche == tab_case[i + 2].coche && 
				tab_case[i].coche != null)
				pos = i;

			if (tab_case[i].coche == tab_case[i + 3].coche && 
				tab_case[i].coche == tab_case[i + 6].coche && 
				tab_case[i].coche != null)
				pos = i + 3;
			
			
		}

		if (tab_case[0].coche == tab_case[4].coche && 
			tab_case[0].coche == tab_case[8].coche && 
			tab_case[0].coche != null)
			pos = 6;
		
		if (tab_case[2].coche == tab_case[4].coche && 
			tab_case[2].coche == tab_case[6].coche && 
			tab_case[2].coche != null)
			pos = 7;
		
		
		if (pos != -1) {
			log("Gagné");
			
			stopGame();
			
		}
		
		return pos;
	}
	
	private void drawPanel(Graphics g) {		
		
		g.setColor(Color.BLACK);
		
		for (int i = 1; i < 3; i++)
			g.drawLine((int) i * GWIDTH / 3, 0, (int) i * GWIDTH / 3, GHEIGHT);
		
		for (int i = 1; i < 3; i++)
			g.drawLine(0, (int) i * GWIDTH / 3, GWIDTH, (int) i * GWIDTH / 3);
		
		int verif = verifSuite();
		
		if (verif != -1) {
			
			g.setColor(Color.RED);
			
			if (verif < 3) {
				g.drawLine(verif * 50 + 50, 0, verif * 50 + 50, GHEIGHT);
			} else if (verif < 6) {
				g.drawLine(0, verif * 50 + 50, GWIDTH, verif * 50 + 50);			
			}
			
			if (verif == 6) {
				g.drawLine(0, 0, GHEIGHT, GHEIGHT);
			}
			
			if (verif == 7) {
				g.drawLine(GHEIGHT, 0, 0, GHEIGHT);
			}
			
			g.setColor(Color.BLACK);
		}
		// SCORE
		
		g.setColor(new Color(200, 100, 200, 80));
		g.fillRect(0, 0, GWIDTH, GHEIGHT / 6);

		g.setColor(Color.BLACK);
		g.drawString("X : " + score[0], 0, 25);
		g.drawString("Y : " + score[0], GWIDTH / 3, 25);
		
		if (verif != -1) {
			String gagnant = (coche == CROIX_SIGNE) ? ROND_SIGNE : CROIX_SIGNE;
			g.drawString("Gagnant: " + gagnant, 2 * GWIDTH / 3, 25);
		}
		
	}
	
	public void addNotify(){
		super.addNotify();
		startGame();
	}
	
	public void startGame(){
		if(game == null || !running){
			game = new Thread(this);
			game.start();
			running = true;
		}
	}
	
	public void stopGame() {
		if (running){
			running = false;
		}
	}
	
	private void log(String s){
		System.out.println(s);
	}

	@Override
	public void run() {
		//<=> Thread.sleep(6);
		long beforeTime, afterTime, diff, sleepTime, overSleepTime = 0;
		int delays = 0;
				
		while (running){
			beforeTime =  System.nanoTime();
			
			gameUpdate();
			gameRender();
			paintScreen();
			
			afterTime = System.nanoTime();
			diff=afterTime-beforeTime;
			sleepTime = (period - diff) - overSleepTime;
			
			timeOperate(beforeTime, afterTime, diff, sleepTime, overSleepTime, delays);
			
		}
		
	}

	void timeOperate(long beforeTime, long afterTime, long diff, 
							long sleepTime, long overSleepTime, int delays) {
		// If the sleep time is between 0 and the period, we can happily sleep
		if ( sleepTime < period && sleepTime > 0){
			try {
				game.sleep(sleepTime / 1000000L);
				overSleepTime = 0;
			} catch (InterruptedException ex) {
				Logger.getLogger(GamePanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else if (diff > period){
			overSleepTime = diff - period;
			
		} else if(++delays >= DELAYS_BEFORE_YIELD){
			game.yield();
			delays = 0;
			overSleepTime = 0;
			
		} else{
			overSleepTime = 0;
		}
		
	}
	
	private void gameUpdate(){
		if (running && game != null){		
			
		}
	}

	private void gameRender(){
		if (dbImage == null) {// create the buffer
			dbImage = createImage(GWIDTH, GHEIGHT);
			if (dbImage == null) {
				System.err.println("dbImage is still null!");
				return;
			}
			else{
				dbg = dbImage.getGraphics();
			}
		}
		//Clear the screen
		dbg.setColor(Color.WHITE);
		dbg.fillRect(0, 0, GWIDTH, GHEIGHT);
		//Draw Game elements
		draw(dbg);
		
	}

	/* Draw all game content in this method */
	private void draw(Graphics g) {
		//Clear the screen
		dbg.setColor(Color.WHITE);
		dbg.fillRect(0, 0, GWIDTH, GHEIGHT);

		drawPanel(g);
		
		for (int i = 0; i < tab_case.length; i++) {
			tab_case[i].draw(g);
		}
		
		
	}
	
	

	private void paintScreen(){
		Graphics g;
		try{
			g = this.getGraphics();
			if (dbImage != null && g != null){
				g.drawImage(dbImage, 0, 0, null);
			}
			Toolkit.getDefaultToolkit().sync(); // for some os such as linux
			g.dispose();
		}catch(Exception e){
			System.err.println(e);
		}
		
	}
	
	
	class Case extends Rectangle {
		
		String coche = null;
		
		
		Case(int i) {
			x = ((int) i / 3) * 100;
			y = ((int) i % 3) * 100 + 10;
			
			height = 100;
			width = 100;
		}
		
		void draw(Graphics g) {
			// g.drawString("X " + x, x, y);
			
			if (coche != null) {
				g.drawString(coche, x + 50, y + 50);
			}
		}
		
		void setCoche(String signe) {
			coche = signe;
		}
		
	}
	
	class MouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			
			
			for (int i = 0; i < tab_case.length; i++) {
				if (tab_case[i].contains(e.getPoint()) && tab_case[i].coche == null) {
					log("Clicked on x=" + e.getX() + " y=" + e.getY());
					tab_case[i].setCoche(coche);
				} else if (tab_case[i].coche != null) {
					
				}
			}
			
			coche = (coche == CROIX_SIGNE) ? ROND_SIGNE : CROIX_SIGNE;
			
			verifSuite();
		}
	}

}
