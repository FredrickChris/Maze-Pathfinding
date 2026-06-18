package game;

import maze.PrimsAlgorithm;
import pathfinding.AStarSearch;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import javax.swing.Timer;

public class Main extends JComponent implements ActionListener, MouseMotionListener, MouseListener, KeyListener{
	
	private Random rand = new Random();
	
	private AStarSearch AStar = new AStarSearch();

	private int width = 1500;
	private int height = 780;

	
	private boolean up = false;
	private boolean down = false;
	private boolean left = false;
	private boolean right = false;
	private boolean shift = false;
	
	
	private int offX = 0;
	private int offY = 0;
	
	private int wallWidth = 50;
	private int side = 25;

	private int pCellX;
	private int pCellY;
	
	private int pSpeed = 4;
	private int pVeloX, pVeloY;
	
	private int eStuckTimer = 0;
	private int eStuckLimit = 60;

	private int eSpeed = 4;
	
	private int targetCellX = 1;
	private int targetCellY = 1;
	
	private int ex = 85*wallWidth + (wallWidth-side)/2;
	private int ey = 19*wallWidth + (wallWidth-side)/2;

	private int eCellX;
	private int eCellY;
	
	private int eVeloX, eVeloY;
	
	private int[][] prox = new int[8][2];
	
	
	private int rowNum = 39;
	private int colNum = 171;
	
	private int[][] map = new PrimsAlgorithm().generateMaze(colNum,rowNum); //must be odd, odd. this is in col, row format
	
	private int endX;
	private int endY;
	
	private boolean revealed = false;
	private int stamina = 120;
	private int srDelay = 0;
	
	private int[][] pathMap = copyMap(map);
	
	private int lastECellX = ex/wallWidth;
	private int lastECellY = ey/wallWidth;

	private int[][] copyMap(int[][] original) {
	    int[][] copy = new int[original.length][original[0].length];
	
	    for (int c = 0; c < original.length; c++) {
	        for (int r = 0; r < original[0].length; r++) {
	            copy[c][r] = original[c][r];
	        }
	    }
	
	    return copy;
	}
	
	private int checkpointX;
	private int checkpointY;
	
	private ArrayList<int[]> objectiveTiles = new ArrayList<>();
	{
		for (int r = 1; r < rowNum-1; r++) {
			for (int c = 1; c < colNum-1; c++) {
				if(map[c][r] == 2) {
					checkpointX = c;
					checkpointY = r;
				}
				if(map[c][r] == 3) {
					endX = c;
					endY = r;
				}
				if(map[c][r] == 4) {
					objectiveTiles.add(new int[] {c,r});
				}
			}
		}
	}

	private int px = checkpointX*wallWidth + (wallWidth-side)/2;
	private int py = checkpointY*wallWidth + (wallWidth-side)/2;
	
	
	Main() {
		setFocusable(true);      // allow focus
	    requestFocusInWindow();  // request focus
	    
	    Timer timer = new Timer(25, e -> {
        	tick(up,down,left,right, shift);
            repaint();   // redraw
        });
        timer.start();
	}
	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paint(g);
	}
	
	
	//================================//
	//            GAME TICK            //
	//================================//
	public void tick(boolean up, boolean down, boolean left, boolean right, boolean shift) {
		
		//check current cell player is in
		pCellX = (px + side/2)/wallWidth;
		pCellY = (py + side/2)/wallWidth;
		
		eCellX = (ex + side/2)/wallWidth;
		eCellY = (ey + side/2)/wallWidth;
		
		if(!(tileCollision(px,py, endX, endY) && !enemyCollision(px, py, ex, ey))) {
			//this gets the surrounding cells
			/*
			 * I could do collision checks for all walls which is more easy to implement 
			 * but the problem with that is that it loops through a large number of walls
			 * every frame (~60 times per second) which will slow down performance. It is
			 * unnecessary to check every existing wall, this array makes records the 
			 * adjacent cells and only checks collision for those walls. 
			 */
			prox[0] = new int[] {pCellX-1, pCellY};
			prox[1] = new int[] {pCellX+1, pCellY};
			prox[2] = new int[] {pCellX, pCellY-1};
			prox[3] = new int[] {pCellX, pCellY+1};
			prox[4] = new int[] {pCellX-1, pCellY-1};
			prox[5] = new int[] {pCellX+1, pCellY-1};
			prox[6] = new int[] {pCellX-1, pCellY+1};
			prox[7] = new int[] {pCellX+1, pCellY+1};
			
			
			//this uses boolean values to change motion
			if(up && !down) {
				if(shift && stamina>0) pVeloY = -(pSpeed+2);		//changes velocity to negative to go up
				else pVeloY = -pSpeed;
			}
			else if(down && !up) {
				if(shift && stamina>0) pVeloY = pSpeed+2;		//changes velocity to negative to go down
				else pVeloY = pSpeed;
			}
			else pVeloY = 0;						//if none or both buttons are pressed, defaults to 0, stops motion
			
			//same here but for x axis
			if(left && !right) {
				if(shift && stamina>0) pVeloX = -(pSpeed + 2);
				else pVeloX = -pSpeed;
			}
			else if(right && !left) {
				if(shift && stamina>0) pVeloX = pSpeed + 2;
				else pVeloX = pSpeed;
			}
			else pVeloX = 0;
			

			boolean inCollisionX = false;
			boolean inCollisionY = false;
			
			//X movement
			px += pVeloX; //increments the player x coordinated based on velocity
			
			for(int i=0; i<prox.length; i++) {	//loops through all walls in proximity
				//check for collision on surrounding cells.
				if(collision(px, py, prox[i][0], prox[i][1])) {
					if (pVeloX > 0) px = prox[i][0]*wallWidth - side; // moving right
					else if (pVeloX < 0) px = prox[i][0]*wallWidth + wallWidth; //moving left
					inCollisionX = true;
				}
			}
			
				
			//Y movement
			py += pVeloY;
			
			for(int i=0; i<prox.length; i++) {
				//check for collision on surrounding cells.
				if(collision(px, py, prox[i][0], prox[i][1])) {
					if (pVeloY > 0) py = prox[i][1]*wallWidth - side; //moving down
					else if (pVeloY < 0) py = prox[i][1]*wallWidth + wallWidth; //moving up
					inCollisionY = true;
				}
			}
			
			//stamina bar
			if(shift && stamina>0 && (((up || down) && !inCollisionY) || ((left || right) && !inCollisionX))) {
				stamina--;
				srDelay=0;
			}
			else {
				srDelay++;
				if(srDelay > 30 && stamina < 120) stamina++;
			}
			
			//World Shift (Aura name bro)
			//X Map
			if(px >= 750 - side/2 && px <= colNum*wallWidth - 750 - side/2) offX = (750 - side/2) - px;
			//Y Map
			if(py >= 350 - side/2 && py <= rowNum*wallWidth - 350 - side/2) offY = (350 - side/2) - py;
			
			
			//ENEMY AI DOWN HERE
				
			//KEY IDEA OF THE LOOP: ish?, changed a lot but similar main concept
			//Search Mode
			//random path finding with player path bias
				//if player is seen (same row/col no wall between) Chase Mode
				
					//if chase mode loses sight of player investigate mode
						//if see player chase
						//if fail to find search
					
				//if player trigger trap or too far Hunt Mode
					//if see player chase mode
					//if lose info search
				
			//Translated to boolean checks:
			if(lineOfSight(pCellX, pCellY, eCellX, eCellY)) {
				pathMap = copyMap(map);
				//chase
				//eSpeed increase
				eSpeed = 5;
				//record last known pCell so if turned false, investigate knows where to go
				targetCellX = pCellX;
				targetCellY = pCellY;
	
				if(targetCellX == eCellX) {
					if(targetCellY < eCellY) {
						for(int i=targetCellY; i<=eCellY; i++) {
							pathMap[targetCellX][i] = 6;
						}
					}
					else {
						for(int i=eCellY; i<=targetCellY; i++) {
							pathMap[targetCellX][i] = 6;
						}
					}
				}
				if(targetCellY == eCellY) {
					if(targetCellX < eCellX) {
						for(int i=targetCellX; i<=eCellX; i++) {
							pathMap[i][targetCellY] = 6;
						}
					}
					else {
						for(int i=eCellX; i<=targetCellX; i++) {
							pathMap[i][targetCellY] = 6;
						}
					}
				}
			}
			else if(inTargetCell(eCellX, eCellY, targetCellX, targetCellY) || far(pCellX, pCellY, targetCellX, targetCellY)) {
				if(revealed || far(pCellX, pCellY, eCellX, eCellY)) { //or too far
					pathMap = copyMap(map);
					//hunt
					//pSpeed increase
					eSpeed = 8;
					//go to trap cell directly
					targetCellX = pCellX;
					targetCellY = pCellY;
					//use A* algorithm to go to trap cell
					AStar.search(targetCellX, targetCellY, eCellX, eCellY, colNum, rowNum, pathMap);
				}
				else {
					pathMap = copyMap(map);
					//search
					eSpeed = 4;
					//random bias
					//check all
					 
				    // check which directions are open paths
				    boolean canUp = map[eCellX][eCellY-1] != 0;
				    boolean canDown = map[eCellX][eCellY+1] != 0;
				    boolean canLeft = map[eCellX-1][eCellY] != 0;
				    boolean canRight = map[eCellX+1][eCellY] != 0;
				    
				    // assign weights based on player direction
				    // player is to the left if pCellX < eCellX, yadayada
				    double wUp, wDown, wLeft, wRight;
	
				    if(canUp) {
				    	if(pCellY < eCellY) wUp = 0.5;
				    	else wUp = 0.2;
				    }
				    else wUp = 0;
	
				    if(canDown) {
				    	if(pCellY > eCellY) wDown = 0.5;
				    	else wDown = 0.2;
				    }
				    else wDown = 0;
	
				    if(canLeft) {
				    	if(pCellX < eCellX) wLeft = 0.5;
				    	else wLeft = 0.2;
				    }
				    else wLeft = 0;
	
				    if(canRight) {
				    	if(pCellX > eCellX) wRight = 0.5;
				    	else wRight = 0.2;
				    }
				    else wRight = 0;
				    
				    double total = wUp + wDown + wLeft + wRight;
				    double roll  = rand.nextDouble() * total;
				    
				    // pick direction based on which range roll falls in
				    if(roll < wUp) {
				        pathMap[eCellX][eCellY-1] = 6;
				        targetCellX = eCellX;
				        targetCellY = eCellY-1;
				    } 
				    else if(roll < wUp + wDown) {
				        pathMap[eCellX][eCellY+1] = 6;
				        targetCellX = eCellX;
				        targetCellY = eCellY+1;
				    } 
				    else if(roll < wUp + wDown + wLeft) {
				        pathMap[eCellX-1][eCellY] = 6;
				        targetCellX = eCellX-1;
				        targetCellY = eCellY;
				    } 
				    else {
				        pathMap[eCellX+1][eCellY] = 6;
				        targetCellX = eCellX+1;
				        targetCellY = eCellY;
				    }
				}
			}
			//move towards target cell but how mark cells and translate towards the next cell until inside it
			//check for adjacent cells with number 2 on pathMap then go there
	
			int centerX = eCellX * wallWidth + (wallWidth - side) / 2;
			int centerY = eCellY * wallWidth + (wallWidth - side) / 2;
			
			
			//only pick new direction when centered
			if(ex > centerX-6 && ex < centerX+6 && ey > centerY-6 && ey < centerY+6) {
				
				if(pathMap[eCellX][eCellY-1] == 6) eVeloY = -eSpeed;		//changes velocity to negative to go up
				else if(pathMap[eCellX][eCellY+1] == 6) eVeloY = eSpeed;	//changes velocity to positive to go down
				else eVeloY = 0;						//if none or both buttons are pressed, defaults to 0, stops motion
				
				//same here but for x axis
				if(pathMap[eCellX-1][eCellY] == 6) eVeloX = -eSpeed;
				else if(pathMap[eCellX+1][eCellY] == 6) eVeloX = eSpeed;
				else eVeloX = 0;
			}
			ey += eVeloY;
			ex += eVeloX;
	
			if(lastECellX != eCellX) pathMap[lastECellX][eCellY] = 1;
			if(lastECellY != eCellY) pathMap[eCellX][lastECellY] = 1;
			
			if(eCellX == lastECellX && eCellY == lastECellY) {
				eStuckTimer++;
			    if(eStuckTimer >= eStuckLimit) {
			    	eStuckTimer = 0;
			    	//makes path re-evaluate
			    	targetCellX = eCellX;
			    	targetCellY = eCellY;
			    }
			} 
			else eStuckTimer = 0; //reset timer
	
			lastECellX = eCellX;
			lastECellY = eCellY;
		}
	}
	
	
	//================================//
	//        HELPER FUNCTIONS        //
	//================================//
	private boolean inTargetCell(int cc, int cr, int tc, int tr) {
		return (
				cc == tc &&
				cr == tr
		);
	}
	
	private boolean lineOfSight(int pCellX, int pCellY, int eCellX, int eCellY) {
		if(pCellX == eCellX) {
			if(pCellY < eCellY) {
				for(int i=pCellY; i<eCellY; i++) {
					if(map[pCellX][i] == 0) return false; //There is a wall between them
				}
			}
			else {
				for(int i=eCellY; i<pCellY; i++) {
					if(map[pCellX][i] == 0) return false; //There is a wall between them
				}
			}
			return true; //no wall
		}
		if(pCellY == eCellY) {
			if(pCellX < eCellX) {
				for(int i=pCellX; i<eCellX; i++) {
					if(map[i][pCellY] == 0) return false; //There is a wall between them
				}
			}
			else {
				for(int i=eCellX; i<pCellX; i++) {
					if(map[i][pCellY] == 0) return false; //There is a wall between them
				}
			}
			return true; //no wall
		}
		return false;
	}
	
	private boolean far(int pCellX, int pCellY, int eCellX, int eCellY) {
		return (
			Math.abs(pCellX-eCellX) > 6 ||
			Math.abs(pCellY-eCellY) > 6
		);
	}
	
	
	public void paint(Graphics g) {
		//Background
		g.setColor(Color.black);
		g.fillRect(offX, offY, colNum*wallWidth, rowNum*wallWidth);
		
		//Path Light
		g.setColor(new Color(200,200,200));
		for(int c = 0; c < colNum; c++) {
			for(int r = 0; r < rowNum; r++) {
				if(map[c][r] == 1) g.fillRect(c*wallWidth + offX, r*wallWidth + offY, wallWidth, wallWidth);
			}
		}
		
		g.setColor(Color.green);
		for(int c = 0; c < colNum; c++) {
			for(int r = 0; r < rowNum; r++) {
				if(map[c][r]==3) g.fillRect(c*wallWidth + offX, r*wallWidth + offY, wallWidth, wallWidth);
			}
		}
		
		//Path Map
		g.setColor(new Color(50,0,50));
		for(int c = 0; c < colNum; c++) {
			for(int r = 0; r < rowNum; r++) {
				if(pathMap[c][r]==6) g.fillRect(c*wallWidth + offX, r*wallWidth + offY, wallWidth, wallWidth);
			}
		}
		
		//Player
		g.setColor(new Color(0,0,100));
		g.fillRect(px + offX, py + offY, side, side);
		
		//Enemy
		g.setColor(new Color(100,0,0));
		g.fillRect(ex + offX, ey + offY, side, side);
		
		//Game Box
		g.setColor(new Color(50,50,50));
		g.fillRect(0, 700, 1500, 80);
		
		//Stamina Bar
		g.setColor(Color.blue);
		g.fillRect(270, 735, stamina*8, 10);
		g.setColor(Color.black);
		g.drawRect(270, 735, 960, 10);
	}
	
	
	//================================//
	//            GAMEPLAY            //
	//================================//
	private boolean collision(int px, int py, int proxX, int proxY) {
		return (
				map[proxX][proxY] == 0 &&
				px < proxX*wallWidth + wallWidth &&
				px + side > proxX*wallWidth &&
				py < proxY*wallWidth + wallWidth &&
				py + side > proxY*wallWidth
		);
	}
	
	
	
	private boolean tileCollision(int px, int py, int tileX, int tileY) {
		return (
				px >= tileX*wallWidth &&
				px + side <= tileX*wallWidth + wallWidth &&
				py >= tileY*wallWidth &&
				py + side <= tileY*wallWidth + wallWidth
		);
	}
	
	
	
	private boolean enemyCollision(int px, int py, int ex, int ey) {
		return (
				px < ex + side &&
				px + side > ex &&
				py < ey + side &&
				py + side > ey
		);
	}
	
	
	public static void main(String args[]) {
		JFrame window = new JFrame("CS4 Java Graphics");
		Main panel = new Main();
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(panel);
		panel.setPreferredSize(new Dimension(panel.width, panel.height));
		window.pack();
		window.setVisible(true);
		window.setLocation(10,10);
		panel.addMouseMotionListener(panel);
		panel.addMouseListener(panel);
		panel.addKeyListener(panel);
	}
	

	//================================//
	//        MOUSE LISTENERS         //
	//================================//
	public void actionPerformed(ActionEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	//================================//
	//          KEY LISTENERS         //
	//================================//
	public void keyTyped(KeyEvent e) {}
	
	public void keyPressed(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_W) {
	        up = true;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_S) {
	        down = true;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_A) {
	        left = true;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_D) {
	        right = true;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
	        shift = true;
	    }
	}

	public void keyReleased(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_W) {
	        up = false;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_S) {
	        down = false;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_A) {
	        left = false;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_D) {
	        right = false;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
	        shift = false;
	    }
	}
}
