package pathfinding;

import java.util.*;
import javax.swing.*;

public class AStarSearch extends JComponent{
	
	public void search(int sc, int sr, int ec, int er, int cols, int rows, int[][] map) {
		
		//initialize parent cells
		int[][] parentC = new int[cols][rows];
		int[][] parentR = new int[cols][rows];
	    
		//add start cell to nodes
		ArrayList<int[]> nodes = new ArrayList<>();
		nodes.add(new int[] {sc,sr,Integer.MIN_VALUE});
		
		int cost=1; //assign a cost, this is the first cost so 1
		
		ArrayList<int[]> open = new ArrayList<>();
		//add opens
		addOpen(nodes, open, map, sc-1, sr, ec, er, cost, cols, rows);
		addOpen(nodes, open, map, sc+1, sr, ec, er, cost, cols, rows);
		addOpen(nodes, open, map, sc, sr-1, ec, er, cost, cols, rows);
		addOpen(nodes, open, map, sc, sr+1, ec, er, cost, cols, rows);
		
		// set parent for initial opens
		parentC[sc-1][sr] = sc; 
		parentR[sc-1][sr] = sr;
		
		parentC[sc+1][sr] = sc; 
		parentR[sc+1][sr] = sr;
		
		parentC[sc][sr-1] = sc; 
		parentR[sc][sr-1] = sr;
		
		parentC[sc][sr+1] = sc; 
		parentR[sc][sr+1] = sr;
		
		//checks if open.size is not 0 and the path has not been found
		while(open.size() > 0) {
			int openIndex = chooseOpen(open);
			
			//add new node
			int nc = open.get(openIndex)[0];
			int nr = open.get(openIndex)[1];
			int nf = open.get(openIndex)[3];
			nodes.add(new int[] {nc,nr,nf});
			
			//check if you have found the best path
			if(nc == ec && nr == er) {
				break;
			}
			
			//declare cost of new opens
			cost = open.get(openIndex)[2] + 1;
			
			//add open
			addOpen(nodes, open, map, nc-1, nr, ec, er, cost, cols, rows);
			addOpen(nodes, open, map, nc+1, nr, ec, er, cost, cols, rows);
			addOpen(nodes, open, map, nc, nr-1, ec, er, cost, cols, rows);
			addOpen(nodes, open, map, nc, nr+1, ec, er, cost, cols, rows);

			if(parentC[nc-1][nr] == 0 && nc-1 != sc) { parentC[nc-1][nr] = nc; parentR[nc-1][nr] = nr; }
			if(parentC[nc+1][nr] == 0 && nc+1 != sc) { parentC[nc+1][nr] = nc; parentR[nc+1][nr] = nr; }
			if(parentC[nc][nr-1] == 0 && nr-1 != sr) { parentC[nc][nr-1] = nc; parentR[nc][nr-1] = nr; }
			if(parentC[nc][nr+1] == 0 && nr+1 != sr) { parentC[nc][nr+1] = nc; parentR[nc][nr+1] = nr; }

			//remove open
			open.remove(openIndex);
		}
		
		//change number to see in map
		for(int i=0; i < nodes.size(); i++) {
			map[nodes.get(i)[0]][nodes.get(i)[1]] = 4;
		}
		for(int i=0; i < open.size(); i++) {
			map[open.get(i)[0]][open.get(i)[1]] = 5;
		}
		
		backtrack(map, parentC, parentR, sc, sr, ec, er);
		
		System.out.println("A* complete");
		
		map[sc][sr] = 3;
		map[ec][er] = 2;
	}
	
	
	private void backtrack(int[][] map, int[][] parentC, int[][] parentR, int sc, int sr, int ec, int er) {
	    int tc = ec, tr = er;
	    //sometimes infinite loops, need this to stop it
	    int limit = map.length * map[0].length;
	    int steps = 0;
	    while(!(tc == sc && tr == sr) && steps++ < limit) {
	        map[tc][tr] = 6;
	        int temp = parentC[tc][tr];
	        tr = parentR[tc][tr];
	        tc = temp;
	    }
        map[sc][sr] = 6; //to mark this cell as well cuz the loop above stops before this
	}
	
	private int chooseOpen(ArrayList<int[]> open) {
		int minPrio = open.get(0)[3];
		int minIndex = 0;
		for(int i=1; i < open.size(); i++) {
			if(open.get(i)[3] < minPrio) {
				minPrio = open.get(i)[3];
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	private void addOpen(ArrayList<int[]> nodes, ArrayList<int[]> open, int[][] map, int oc, int or, int ec, int er, int cost, int cols, int rows) {
		if (oc <= 0 || oc >= cols || or <= 0 || or >= rows) return;
		
		//checks if it already exists as a node
		for(int i=0; i < nodes.size(); i++) {
			if(nodes.get(i)[0] == oc && nodes.get(i)[1] == or) {
				return; //stops program if it is
			}
		}
		
		//checks if it is an existing node
		for(int i=0; i < open.size(); i++) {
			if(
					open.get(i)[0] == oc &&		//same col
					open.get(i)[1] == or &&		//same row
					(map[oc][or] == 1 || map[oc][or] == 2) 
			) {
				if(open.get(i)[2] > cost) {		//smaller cost
					//if smaller cost, replace
					open.set(i, new int[] {oc, or, cost, priorityScore(oc, or, ec, er, cost)});
				}
				//then return
				return; //stops after updating
			}
		}
		
		//checks if it is a path and is within bound
		if(
				oc > 0 		&&
				oc < cols 	&&
				or > 0 		&&
				or < rows 	&&
				(map[oc][or] == 1 || map[oc][or] == 2)
		) {
			open.add(new int[] {oc, or, cost, priorityScore(oc, or, ec, er, cost)});
			return;
		}
		
		//if all is passed, return
		return;
	}
	
	private int priorityScore(int cellCol, int cellRow, int endCol, int endRow, int cost) {
		return cost + (Math.abs(cellCol - endCol) + Math.abs(cellRow - endRow));
	}
}
