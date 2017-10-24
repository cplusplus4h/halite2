import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import hlt.Constants;
import hlt.DebugLog;
import hlt.DockMove;
import hlt.Entity;
import hlt.GameMap;
import hlt.Move;
import hlt.Navigation;
import hlt.Networking;
import hlt.Planet;
import hlt.Ship;

public class MyBot {

	public static void main(final String[] args) {
		final Networking networking = new Networking();
		final GameMap gameMap = networking.initialize("harshwinds v4");

		final ArrayList<Move> moveList = new ArrayList<>();
		for (;;) {
			moveList.clear();
			gameMap.updateMap(Networking.readLineIntoMetadata());
			
			Collection<Ship> muyShips = gameMap.getMyPlayer().getShips().values();
			Collection<Ship> undockedShips = muyShips.stream()
						.filter(ship -> ship.getDockingStatus() == Ship.DockingStatus.Undocked)
						.collect(Collectors.toList());
			
			DebugLog.addLog("-" + undockedShips.size() + " UNDOCKED SHIPS-");

			for (final Ship undockedShip : undockedShips) {
				DebugLog.addLog("-BEGIN UNDOCKED SHIP " + undockedShip.getId() + "-");
				Move move = null;
				
				Collection<Entity> entitiesByDistance = gameMap.nearbyEntitiesByDistance(undockedShip).values();
				
				List<Planet> unownedPlanetsByDistance = entitiesByDistance.stream()
						.filter(entity -> entity instanceof Planet)
						.map(planet -> (Planet) planet)
						.filter(planet -> !planet.isOwned())
						.collect(Collectors.toList());
				
				DebugLog.addLog("-" + unownedPlanetsByDistance.size() + " UNOWNED PLANETS-");

				// Attempt interactions with planets first because we want to mine so we can produce new ships
				for (int planetIndex = 0; planetIndex < unownedPlanetsByDistance.size() && move == null; planetIndex++) {
					Planet planet = unownedPlanetsByDistance.get(planetIndex);
					
					// Dock, if possible
					if (undockedShip.canDock(planet)) {
						DebugLog.addLog("-WILL DOCK " + planet.getId() + "-");
						move = planet.dock(undockedShip);
					}

					// Otherwise attempt to move towards the planet
					if (move == null) {
						DebugLog.addLog("-ATTEMPTING TO MOVE TOWARDS " + planet.getId() + "-");
						move = Navigation.navigateShipToDock(gameMap, undockedShip, planet, Constants.MAX_SPEED);
					}
				}
				
				// Unable to effectively move toward the planet, so let's take on enemy ships
				if (move == null) {
					DebugLog.addLog("-NO PLANETS TO MOVE TOWARDS, LOOKING FOR SHIPS-");
					List<Ship> enemyShipsByDistance = entitiesByDistance.stream()
							.filter(entity -> entity instanceof Ship)
							.map(enemyShip -> (Ship) enemyShip)
							.filter(enemyShip -> enemyShip.getOwner() != gameMap.getMyPlayerId())
							.collect(Collectors.toList());
					
					DebugLog.addLog("-" + enemyShipsByDistance.size() + " ENEMY SHIPS-");

					for (int shipIndex = 0; shipIndex < enemyShipsByDistance.size() && move == null; shipIndex++) {
						Ship enemyShip = enemyShipsByDistance.get(shipIndex);
						
						DebugLog.addLog("-ATTEMPTING TO MOVE TOWARDS " + enemyShip.getId() + "-");
						move = Navigation.navigateShipTowardsTarget(gameMap, undockedShip, undockedShip.getClosestPoint(enemyShip), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
					}
				}
				
				// Still, may not have been an effective path.  Stay put for now.
				if (move == null) {
					DebugLog.addLog("-NO SHIPS TO MOVE TOWARDS, STAYING PUT-");
					// throw new IllegalArgumentException("No move for ship!");
					// Should we try and take on ships we already own?  Enemies own?
				} else {
					moveList.add(move);
				}
				DebugLog.addLog("-END UNDOCKED SHIP " + undockedShip.getId() + "-");
			}
			
			Networking.sendMoves(moveList);
		}
	}
}
