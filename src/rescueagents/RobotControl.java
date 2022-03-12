package rescueagents;

import rescueframework.AbstractRobotControl;
import world.Cell;
import world.Path;
import world.Robot;
import world.RobotPercepcion;

import java.util.ArrayList;

import world.AStarSearch;
import world.Injured;

/**
 * RobotControl class to implement custom robot control strategies
 */
public class RobotControl extends AbstractRobotControl {
	/**
	 * Default constructor saving world robot object and percepcion
	 * 
	 * @param robot      The robot object in the world
	 * @param percepcion Percepcion of all robots
	 */
	public RobotControl(Robot robot, RobotPercepcion percepcion) {
		super(robot, percepcion);
	}

	/**
	 * Custom step strategy of the robot, implement your robot control here!
	 * 
	 * @return Return NULL for staying in place, 0 = step up, 1 = step right, 2 =
	 *         step down, 3 = step left
	 */
	@SuppressWarnings("null")
	public Integer step() {
		// By default the robot stays in place
		// a cella, ahol a robot most van
		Cell actLoc = robot.getLocation();
		// legrövidebb út az áldozathoz
		Path injuredPath = percepcion.getShortestInjuredPath(actLoc);
		// legrövidebb út egy ismeretlen mezőig
		Path unknowPath = percepcion.getShortestUnknownPath(actLoc);
		// legrövidebb út a kijáratig
		Path exitPath = percepcion.getShortestExitPath(actLoc);
		
		ArrayList<Robot> robots = percepcion.getRobots();
		
		boolean sokRobot = robots.size() > 1;
		
		robot.getLocation().getAccessibleNeigbour(0);
		
		// felderített áldozatok
		ArrayList<Injured> discoveredInjureds = percepcion.getDiscoveredInjureds();
		ArrayList<Injured> injureds = new ArrayList<>();
		for(int i = 0; i < discoveredInjureds.size(); i++) {
			if(!discoveredInjureds.get(i).isSaved()) { // ha nincs megmentve
				System.out.println(i + ". aldozat elete: " + discoveredInjureds.get(i).getHealth());
				injureds.add(discoveredInjureds.get(i));
			}
		}
		
		// minimalis eletu kivalasztasa
		// ebbe fogjuk tarolni a legserultebb aldozatot
		Injured minHealthInjured = null;
		// felfedezett aldozatok osszes eletereje
		int sumHealth = 0;
		// alapertelmezett az elso, ha van
		if(injureds.size() > 0) {
			if(injureds.get(0) != null && injureds.get(0).isAlive()) {
				minHealthInjured = injureds.get(0);
				sumHealth += injureds.get(0).getHealth();
			}
		}
		// megnezzuk a tobbit is ha van
		for(int i = 1; i < injureds.size(); i++) {
			if(minHealthInjured == null) {
				minHealthInjured = injureds.get(i);
			} else if(minHealthInjured.getHealth() > injureds.get(i).getHealth() && injureds.get(0).isAlive()) {
				minHealthInjured = injureds.get(i);
			}
			sumHealth += injureds.get(i).getHealth();
		}
		
		Path minHealthInjuredPath = null;
		if(minHealthInjured != null && minHealthInjured.getLocation() != null) {
			minHealthInjuredPath = AStarSearch.search(actLoc,minHealthInjured.getLocation(), -1);
			System.out.println("Min health: " + minHealthInjured.getHealth() + "SUM: " + sumHealth);
		}
		
		// mivel az int nem lehet null, 
		// ezért egy irányoktól különböző számot írtam alapértelmezett lépésértéknek
		int step = -1;
		
		if(minHealthInjuredPath != null) {
			injuredPath = minHealthInjuredPath; // ha van betegebb aldozzathoz utvonalunk, akkor oda igyekezzen
		}
		
		if (!robot.hasInjured()) {	// Ha nincs nála áldozat
			if (injuredPath != null) { // áldozat felé
				if( unknowPath != null && (sumHealth == 0 || (sokRobot && injuredPath.getLength() > unknowPath.getLength() && (unknowPath.getLength() <= 5 || injuredPath.getLength() > 15)))) {
					Cell unkLoc = unknowPath.getFirstCell();
					if (actLoc.getX() == unkLoc.getX() && actLoc.getY() > unkLoc.getY())
						step = 0;
					if (actLoc.getX() == unkLoc.getX() && actLoc.getY() < unkLoc.getY())
						step = 2;
					if (actLoc.getX() < unkLoc.getX() && actLoc.getY() == unkLoc.getY())
						step = 1;
					if (actLoc.getX() > unkLoc.getX() && actLoc.getY() == unkLoc.getY())
						step = 3;
				} else {
					Cell injLoc = injuredPath.getFirstCell();
					if (actLoc.getX() == injLoc.getX() && actLoc.getY() > injLoc.getY())
						step = 0;
					else if (actLoc.getX() == injLoc.getX() && actLoc.getY() < injLoc.getY())
						step = 2;
					else if (actLoc.getX() < injLoc.getX() && actLoc.getY() == injLoc.getY())
						step = 1;
					else if (actLoc.getX() > injLoc.getX() && actLoc.getY() == injLoc.getY())
						step = 3;
				}
			} else if (unknowPath != null) { // szürke mező felé
				Cell unkLoc = unknowPath.getFirstCell();
				if (actLoc.getX() == unkLoc.getX() && actLoc.getY() > unkLoc.getY())
					step = 0;
				if (actLoc.getX() == unkLoc.getX() && actLoc.getY() < unkLoc.getY())
					step = 2;
				if (actLoc.getX() < unkLoc.getX() && actLoc.getY() == unkLoc.getY())
					step = 1;
				if (actLoc.getX() > unkLoc.getX() && actLoc.getY() == unkLoc.getY())
					step = 3;
			}
		} else {	// Ha van nála áldozat
			if (exitPath != null) { // kijárat felé
				Cell exitLoc = exitPath.getFirstCell();
				if (actLoc.getX() == exitLoc.getX() && actLoc.getY() > exitLoc.getY())
					step = 0;
				else if (actLoc.getX() == exitLoc.getX() && actLoc.getY() < exitLoc.getY())
					step = 2;
				else if (actLoc.getX() < exitLoc.getX() && actLoc.getY() == exitLoc.getY())
					step = 1;
				else if (actLoc.getX() > exitLoc.getX() && actLoc.getY() == exitLoc.getY())
					step = 3;
			} else if (unknowPath != null) { // szürke mező felé
				Cell unkLoc = unknowPath.getFirstCell();
				if (actLoc.getX() == unkLoc.getX() && actLoc.getY() > unkLoc.getY())
					step = 0;
				if (actLoc.getX() == unkLoc.getX() && actLoc.getY() < unkLoc.getY())
					step = 2;
				if (actLoc.getX() < unkLoc.getX() && actLoc.getY() == unkLoc.getY())
					step = 1;
				if (actLoc.getX() > unkLoc.getX() && actLoc.getY() == unkLoc.getY())
					step = 3;
			}
		}
		
		// ha nincs már cél a robot megáll
		if (step == -1)
			return null;
		// különben lép
		else
			return step;
	}
}
