package net.clayrobot.delivery.entities;

import static net.clayrobot.delivery.entities.Entity.entities;

public class Entities {
	public static void draw(float deltaTime) {
		for (Entity entity : entities) {
			entity.draw(deltaTime);
		}	
	}
	public static void clear() {
		for (Entity entity : entities) {
			entity.delete();
		}
		entities.clear();
	}
	public static void disposeStaticResources() {
		clear();
		Player.dispose();
		Box.dispose();
	}
}
