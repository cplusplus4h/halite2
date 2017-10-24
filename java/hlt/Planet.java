package hlt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Planet extends Entity {

    private final int remainingProduction;
    private final int currentProduction;
    private final int dockingSpots;
    private final List<Integer> dockedShips;
    private final List<DockMove> dockMoves;

    public Planet(final int owner, final int id, final double xPos, final double yPos, final int health,
                  final double radius, final int dockingSpots, final int currentProduction,
                  final int remainingProduction, final List<Integer> dockedShips) {

        super(owner, id, xPos, yPos, health, radius);

        this.dockingSpots = dockingSpots;
        this.currentProduction = currentProduction;
        this.remainingProduction = remainingProduction;
        this.dockedShips = Collections.unmodifiableList(dockedShips);
        dockMoves = new ArrayList<>();
    }

    public int getRemainingProduction() {
        return remainingProduction;
    }

    public int getCurrentProduction() {
        return currentProduction;
    }

    public int getDockingSpots() {
        return dockingSpots;
    }

    public List<Integer> getDockedShips() {
        return dockedShips;
    }

    public boolean isFull() {
        return dockedShips.size() + dockMoves.size() == dockingSpots;
    }

    public boolean isOwned() {
        return getOwner() != -1;
    }
    
    public DockMove dock(Ship ship) {
    		if (!isFull()) {
	    		DockMove dockMove = new DockMove(ship, this);
	    		dockMoves.add(dockMove);
	    		return dockMove;
    		} else {
    			return null;
    		}
    }

    @Override
    public String toString() {
        return "Planet[" +
                super.toString() +
                ", remainingProduction=" + remainingProduction +
                ", currentProduction=" + currentProduction +
                ", dockingSpots=" + dockingSpots +
                ", dockedShips=" + dockedShips +
                "]";
    }
}
