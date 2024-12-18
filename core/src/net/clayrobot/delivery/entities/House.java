package net.clayrobot.delivery.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import net.clayrobot.delivery.AutobahnDelivery;
import net.clayrobot.delivery.levels.Level;

public class House extends Entity {
	private final Body body;
	private final Texture houseTex = new Texture("house/house.png");
	private final Sprite houseSprite = new Sprite(houseTex);
	private final Sprite addressSprite;
	private final Sprite checkSprite = new Sprite(new Texture("house/check.png"));
	private static int count = 0; // number of houses
	public final int address; // like a street address
	private static House[] houseAt; // array for getting house at address
	private int boxesHere = 0;
	private int boxesNeeded = 0;
	private final int hitboxHeight = 6;
	FixtureDef fixtureDef = new FixtureDef();
	private House(float x, float y, float width, float height, int address) {
		this.address = address;
		game.staticBodyDef.position.set(x, y + hitboxHeight / 2);
		body = Level.world.createBody(game.staticBodyDef);
		
		PolygonShape square = new PolygonShape();
		square.setAsBox(width / 2, hitboxHeight / 2);
		fixtureDef.shape = square;
		fixtureDef.isSensor = true; // this means the house is not a physical object but just a region that can be entered (for contact listener)
		body.setUserData(this); // this is so the box2d contact listener can get a reference to the object from the body
		body.createFixture(fixtureDef);
		width *= 1.2f;
		height *= 1.2f;
		houseSprite.setBounds(x - width / 2, y - 0.3f, width, height);
		
		
		checkSprite.setBounds(x - width / 2, y - 0.3f, width, height);
		addressSprite = new Sprite(new Texture("house/" + address + ".png"));
		addressSprite.setBounds(x - width / 2, y - 0.3f, width, height);
		update();
	}
	
	@Override
	public void draw(float deltaTime) {
		houseSprite.draw(game.batch);
		addressSprite.draw(game.batch);
		if (boxesHere >= boxesNeeded) {
			checkSprite.draw(game.batch); // draws checkmark if delivery is fulfilled
		}
	}
	@Override
	protected void update() {
		/*if (boxesHere >= boxesNeeded) {
			houseSprite.setTexture(greenHouseTex);
		}
		else {
			houseSprite.setTexture(houseTex);
		}*/
	}
	public void incrementScore() { // these score functions are called by the contact listener when appropriate
		boxesHere++;
		update();
	}
	public void decrementScore() {
		boxesHere--;
		update();
	}
	@Override
	protected void delete() {
		houseTex.dispose();
		addressSprite.getTexture().dispose();
		checkSprite.getTexture().dispose();
		houseAt[address] = null;
		Level.world.destroyBody(body);
	}
	protected static int assignAddress() { // method called by Box.spawn() to pass a suitable address to a new box's constructor
		AutobahnDelivery game = AutobahnDelivery.getGame();
		int address = game.random.nextInt(count) + 1;
		houseAt[address].boxesNeeded++; // increments that house's required boxes since a box was just assigned its address
		return address;
	}
	public static void spawn(MapObjects mapObjects) { // this method spawns houses with their specified sizes and locations in a MapObjects array
		count = mapObjects.getCount();
		houseAt = new House[count + 1];
		for (int i = 0; i < count; i++) {
			MapObject mapObject = mapObjects.get(i);
			Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
			houseAt[i + 1] = new House(rect.x / 4 + rect.width / 8, rect.y / 4, rect.width / 4, rect.height / 4, i + 1);
		}
	}
	public static void spawn(int houseCount) { // this method spawns houses in a row
		count = houseCount;
		houseAt = new House[count + 1];
		for (int i = 0; i < count; i++) {
			houseAt[i + 1] = new House(40 + 15 * i, 8, 10, 10, i + 1);
		}
	}
	protected static void dispose() {
		//for (int i = 1; i < addressTex.length; i++) {
			//addressTex[i].dispose();
		//}
	}
}
