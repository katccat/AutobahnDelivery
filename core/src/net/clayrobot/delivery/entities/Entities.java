package net.clayrobot.delivery.entities;

import com.badlogic.gdx.utils.Array;

public class Entities {
	protected static final Array<Entity> entities = new Array<>();
	public static void draw(float deltaTime) { // this method is called to draw all entities in entities array
		for (Entity entity : entities) {
			entity.draw(deltaTime);
		}	
	}
	public static void clear() { // this method deletes all entities (like when restarting the level)
		for (Entity entity : entities) {
			entity.delete();
		}
		Box.clear(); // box also has a reference list so clear that also
		entities.clear();
	}
	public static void printEntities() {
		System.out.println(entities.toString());
	}
	public static void disposeStaticResources() { // called when quitting game
		clear();
		Player.dispose();
		Box.dispose();
		House.dispose();
	}
}
