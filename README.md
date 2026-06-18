# Maze Chase

A top-down 2D maze game built in Java using Swing/AWT. The player navigates a procedurally generated maze while being pursued by an AI enemy that dynamically switches between search, chase, and hunt behaviors. The project is also a real-time visualization of **Prim’s Maze Generation** and **A* Pathfinding** applied inside a gameplay loop.

---

## Related Projects

The core systems in this game were originally built as standalone visualizers before being integrated:

* **[Prim's Algorithm Visualizer](https://github.com/FredrickChris/Prims-Algorithm)** — original implementation of the maze generation logic.
* **[A* Search Visualizer](https://github.com/FredrickChris/A-Star-Search)** — original implementation of the pathfinding system.

Both algorithms are now embedded in this project under:

* `maze/PrimsAlgorithm`
* `pathfinding/AStarSearch`

---

## Controls

| Key     | Action                    |
| ------- | ------------------------- |
| `WASD`  | Move player               |
| `Shift` | Sprint (consumes stamina) |

---

## Map Legend

| Value | Meaning                           |
| ----- | --------------------------------- |
| `0`   | Wall                              |
| `1`   | Path                              |
| `2`   | Player spawn / checkpoint         |
| `3`   | Exit                              |
| `4`   | Objective tile                    |
| `6`   | AI path visualization (temporary) |

---

## Maze Generation (Prim’s Algorithm)

The maze is generated in multiple stages:

1. **Grid Initialization**

   * All cells start as walls (`0`)
   * Odd-indexed cells become initial path nodes (`1`), forming a grid skeleton

2. **Randomized Prim’s Carving**

   * Expands from a starting node
   * Randomly connects frontier cells to the existing maze
   * Ensures full connectivity with no isolated regions

3. **Braiding (Loop Creation)**

   * Scans wall cells and selectively removes them
   * Only removes walls that connect two opposite open paths
   * Applied with a **10% probability**
   * Converts the perfect maze into a looped, more natural structure

4. **Start & Exit Placement**

   * Places spawn (`2`) and exit (`3`) in dead-end regions
   * Ensures structured entry/exit flow

---

## Core Gameplay Systems

### Movement & Collision

* Uses **local proximity collision checks (8 surrounding cells)** instead of full-grid scanning
* Improves performance for large mazes
* Grid-based movement with pixel interpolation

### Stamina System

* Sprint increases speed (`+2`)
* Stamina drains while sprinting
* Regenerates after short delay
* Visualized as a UI bar

### Camera System

* World uses offset-based rendering (`offX`, `offY`)
* Player remains centered using a soft dead-zone camera

### Win Condition

* Player must reach exit tile (`3`)
* Must avoid collision with enemy at the same time

---

## Enemy AI System

The AI operates using a **state-driven system** evaluated every tick.

### 1. Chase Mode (Line of Sight)

Triggered when:

* Player is in the same row or column
* No wall blocks direct vision

Behavior:

* Speed increases
* Enemy directly targets player’s current position
* Straight-line path is visualized in real time

---

### 2. Hunt Mode (A* Pathfinding)

Triggered when:

* Player is lost but recently seen, or
* Player is far from last known position

Behavior:

* Uses **A* search** to compute shortest path
* Targets player’s last known (or current) position
* Speed significantly increases
* Path is recalculated dynamically

---

### 3. Search Mode (Exploration)

Fallback behavior when no target is active:

* Evaluates adjacent walkable tiles
* Uses **weighted random movement**
* Biases movement toward player’s last known direction
* Prevents idle or repetitive movement patterns

---

## Movement Logic

* Enemy only updates direction when centered in a tile
* Prevents diagonal clipping or partial tile movement
* Includes **stuck detection system**

  * If no movement occurs for 60 ticks, AI resets its decision state

---

## A* Pathfinding (Hunt Mode)

The A* implementation follows standard grid-based logic:

* **Cost function:** `f(n) = g(n) + h(n)`

  * `g(n)` = distance traveled
  * `h(n)` = Manhattan distance heuristic

* **Movement:** 4-directional only (no diagonals)

* **Open set:** frontier nodes to explore

* **Closed set:** already evaluated nodes

* **Parent tracking:** reconstructs final path

### Key Features:

* Replaces higher-cost paths when found
* Stops when target node is expanded
* Backtracks to build final path
* Marks path as `6` for visualization

---

## Project Structure

* `game/Main.java` → Core loop, rendering, input, and AI logic
* `maze/PrimsAlgorithm.java` → Maze generation system
* `pathfinding/AStarSearch.java` → AI pathfinding during hunt mode

---

## Purpose

This project demonstrates:

* Real-time **Prim’s Algorithm maze generation**
* Practical **A* pathfinding in dynamic gameplay**
* Hybrid AI combining:

  * deterministic vision-based chase
  * heuristic search (A*)
  * probabilistic exploration
