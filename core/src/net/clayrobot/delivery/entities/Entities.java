package net.clayrobot.delivery.entities;

import com.badlogic.gdx.utils.Array;

public class Entities {
	protected static final Array<Entity> entities = new Array<>();
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
	public static void printEntities() {
		System.out.println(entities.toString());
	}
	public static void disposeStaticResources() {
		clear();
		Player.dispose();
		Box.dispose();
		House.dispose();
	}
}
