package maze;

import java.util.*;
import javax.swing.*;
import java.util.ArrayList;

public class PrimsAlgorithm extends JComponent {
	
	private Random rand = new Random();

	//================================//
	//            METHODS             //
	//================================//
	
    public int[][] generateMaze(int cols, int rows) {
		//fill the map with walls
		int[][] map = new int[cols][rows];
		for(int c = 0; c < cols; c++) {
			for(int r = 0; r < rows; r++) {
				map[c][r] = 0;
			}
		}
		//create grid intervals
		for(int c = 1; c < cols - 1; c+=2) {
			for(int r = 1; r < rows - 1; r+=2) {
				map[c][r] = 1;
			}
		}

		//choose random path block
		int ic = rand.nextInt((cols-1)/2)*2+1;
		int ir = rand.nextInt((rows-1)/2)*2+1;
		
		ArrayList<int[]> nodes = new ArrayList<>();
		nodes.add(new int[] {ic,ir});
		
		ArrayList<int[]> fronts = new ArrayList<>();
		if(checkFront(ic-2,ir,cols,rows)) fronts.add(new int[] {ic-2,ir});
		if(checkFront(ic+2,ir,cols,rows)) fronts.add(new int[] {ic+2,ir});
		if(checkFront(ic,ir-2,cols,rows)) fronts.add(new int[] {ic,ir-2});
		if(checkFront(ic,ir+2,cols,rows)) fronts.add(new int[] {ic,ir+2});
		
		
		//looping through all nodes to check if all grids had been connected
		int gridNum = ((cols-1)/2)*((rows-1)/2);
		while(nodes.size() < gridNum) {
			//Pick a random coordinate from fronts.
			int frontIndex = rand.nextInt(fronts.size());
			
			//Check if that coordinate is already in nodes (to avoid loops/duplicates).
			if(!checkFront(nodes, fronts.get(frontIndex))) { // true if front is a node, must not be an existing node.
				//Carve the wall between the new node and the existing maze.
				//check all adjacent nodes
				ArrayList<int[]> adjNodes = new ArrayList<>();
				int fc = fronts.get(frontIndex)[0];
				int fr = fronts.get(frontIndex)[1];
				for(int i=0; i<nodes.size(); i++) {
					if(nodes.get(i)[0] == fc-2 && nodes.get(i)[1] == fr) adjNodes.add(new int[] {fc-2,fr});
					if(nodes.get(i)[0] == fc+2 && nodes.get(i)[1] == fr) adjNodes.add(new int[] {fc+2,fr});
					if(nodes.get(i)[0] == fc && nodes.get(i)[1] == fr-2) adjNodes.add(new int[] {fc,fr-2});
					if(nodes.get(i)[0] == fc && nodes.get(i)[1] == fr+2) adjNodes.add(new int[] {fc,fr+2});
				}
				//randomize which node to carve to.
				int nodeIndex = rand.nextInt(adjNodes.size());
				int nc = adjNodes.get(nodeIndex)[0];
				int nr = adjNodes.get(nodeIndex)[1];
				
				//check y and x if +/- 2 and make it 1 and change to path
				map[fc+(nc-fc)/2][fr+(nr-fr)/2] = 1;
				
				
				//Add the new node to the nodes list.
				nodes.add(fronts.get(frontIndex));
				
				//update fronts
				int newc = fronts.get(frontIndex)[0];
				int newr = fronts.get(frontIndex)[1];
				if(checkFront(newc-2,newr,cols,rows)) fronts.add(new int[] {newc-2,newr});
				if(checkFront(newc+2,newr,cols,rows)) fronts.add(new int[] {newc+2,newr});
				if(checkFront(newc,newr-2,cols,rows)) fronts.add(new int[] {newc,newr-2});
				if(checkFront(newc,newr+2,cols,rows)) fronts.add(new int[] {newc,newr+2});
				
				//remove the front
				fronts.remove(frontIndex);
			}
		}
		
		braid(map, cols, rows);
		addStart(map);
		addExit(map, cols, rows);
		
		return map;
	}
	
	
	private void braid(int[][] map, int cols, int rows) {
		for (int c = 1; c < cols-1; c++) {
			for (int r = 1; r < rows-1; r++) {
				//check for walls, can only break if 2 opposite sides is a path. c+-1 or r+-1
				if (
					map[c][r] == 0 && //is a wall
					(
						(	//opposite rows path, not opposite columns
							map[c-1][r] == 1 && 
							map[c+1][r] == 1 && 
							map[c][r-1] != 1 && 
							map[c][r+1] != 1
						) || 
						(	//opposite columns path, not opposite rows
							map[c-1][r] != 1 && 
							map[c+1][r] != 1 && 
							map[c][r-1] == 1 && 
							map[c][r+1] == 1
						)
					) &&
					rand.nextDouble() < 0.10  //10% chance
					
				) {
					map[c][r] = 1;
				}
			}
		}
	}

	
	
	private void addExit(int[][] map, int cols, int rows) {
		ArrayList<int[]> possibleExit = new ArrayList<>();
		for (int c = cols-7; c < cols-1; c++) {
			for (int r = 30; r < rows-1; r++) {
				if(
					map[c][r] == 1 &&
					(
						(map[c-1][r] == 1 && map[c+1][r] == 0 && map[c][r-1] == 0 && map[c][r+1] == 0) ||
						(map[c-1][r] == 0 && map[c+1][r] == 1 && map[c][r-1] == 0 && map[c][r+1] == 0) ||
						(map[c-1][r] == 0 && map[c+1][r] == 0 && map[c][r-1] == 1 && map[c][r+1] == 0) ||
						(map[c-1][r] == 0 && map[c+1][r] == 0 && map[c][r-1] == 0 && map[c][r+1] == 1)
					)
				) possibleExit.add(new int[] {c,r});
			}
		}
		int[] exit = possibleExit.get(rand.nextInt(possibleExit.size()));
		
		map[exit[0]][exit[1]] = 3;
	}
	
	private void addStart(int map[][]) {
		ArrayList<int[]> StartList = new ArrayList<>();
		for (int c = 1; c < 11; c++) {
			for (int r = 1; r < 15; r++) {
				if(
					map[c][r] == 1 &&
					(
						(map[c-1][r] == 1 && map[c+1][r] == 0 && map[c][r-1] == 0 && map[c][r+1] == 0) ||
						(map[c-1][r] == 0 && map[c+1][r] == 1 && map[c][r-1] == 0 && map[c][r+1] == 0) ||
						(map[c-1][r] == 0 && map[c+1][r] == 0 && map[c][r-1] == 1 && map[c][r+1] == 0) ||
						(map[c-1][r] == 0 && map[c+1][r] == 0 && map[c][r-1] == 0 && map[c][r+1] == 1)
					)
				) StartList.add(new int[] {c,r});
			}
		}
		int[] start = StartList.get(rand.nextInt(StartList.size()));
		map[start[0]][start[1]] = 2;
	}
	
	
	private boolean checkFront(int frontC, int frontR, int cols, int rows) {
		return (
				frontC > 0 &&
				frontC < cols &&
				frontR > 0 &&
				frontR < rows
				);
	}
	
	
	private boolean checkFront(ArrayList<int[]> nodes, int[] front) {  //returns true if front is an existing node
		for(int i=0; i < nodes.size(); i++) {
			if(nodes.get(i)[0] == front[0] && nodes.get(i)[1] == front[1]) {
				return true;
			}
		}
		return false;
	}
}
