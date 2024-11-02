package net.clayrobot.delivery.entities;

import net.clayrobot.delivery.AutobahnDelivery;
import static net.clayrobot.delivery.entities.Entities.entities;

abstract class Entity {
	
	protected AutobahnDelivery game;
	public Entity() {
		entities.add(this);
		game = AutobahnDelivery.getGame();
	}
	protected void draw(float deltaTime) {
		if (deltaTime > 0) this.update();
	}
	abstract protected void update();
	abstract protected void delete();
}