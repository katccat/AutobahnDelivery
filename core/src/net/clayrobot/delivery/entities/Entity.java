package net.clayrobot.delivery.entities;

import net.clayrobot.delivery.AutobahnDelivery;
import static net.clayrobot.delivery.entities.Entities.entities;

abstract class Entity {
	
	protected AutobahnDelivery game;
	public Entity() {
		entities.add(this); // subclasses of Entity implicitly call this so they can be kept track of
		game = AutobahnDelivery.getGame();
	}
	protected void draw(float deltaTime) { // renders entity on screen
		if (deltaTime > 0) this.update(); // only update if time has passed (don't update during pause)
	}
	abstract protected void update(); // updates entity's state
	abstract protected void delete(); // entities will need to be deleted for various reasons (no longer needed, game restarting, quitting level, etc)
}