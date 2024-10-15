package net.clayrobot.delivery.entities;

import com.badlogic.gdx.utils.Array;
import net.clayrobot.delivery.Delivery;

class Entity {
	protected static final Array<Entity> entities = new Array<>();
	protected Delivery game;
	public Entity() {
		entities.add(this);
		game = Delivery.getGame();
	}
	protected void update() {
		
	}
	protected void draw(float deltaTime) {
		if (deltaTime > 0) this.update();
	}
	protected void delete() {
		
	}
	protected static void dispose() {
		
	}
}