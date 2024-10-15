package net.clayrobot.delivery.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import net.clayrobot.delivery.Delivery;

public class House extends Entity {
	private final Body body;
	private Color color = Color.ORANGE;
	private final Vector2 position;
	private final Rectangle RECTANGLE;
	private final Texture houseTex = new Texture("house2.png");
	private final Texture greenHouseTex = new Texture("green_house2.png");
	private final Sprite houseSprite = new Sprite(houseTex);
	private static int count = 0;
	public final int address;
	private static House[] houseAt;
	private int boxesHere = 0;
	private int boxesNeeded = 0;
	FixtureDef fixtureDef = new FixtureDef();
	private House(float x, float y, float width, float height, int address) {
		this.address = address;
		game.staticBodyDef.position.set(x, y + height / 2);
		body = game.activeWorld.createBody(game.staticBodyDef);
		
		PolygonShape square = new PolygonShape();
		square.setAsBox(width / 2, height / 2);
		fixtureDef.shape = square;
		fixtureDef.isSensor = true;
		body.setUserData(this);
		body.createFixture(fixtureDef);
		position = new Vector2(x, y);
		RECTANGLE = new Rectangle(x - width / 2, y, width, height);
		width *= 1.2f;
		height *= 1.2f;
		houseSprite.setBounds(x - width / 2, y - 0.3f, width, height);
	}
	
	@Override
	public void draw(float deltaTime) {
		houseSprite.draw(game.batch);
		game.InstrumentSerif.draw(game.batch, String.valueOf(address), position.x - 2.51f, position.y + 10f);
	}
	public void update() {
		if (boxesHere >= boxesNeeded) {
			houseSprite.setTexture(greenHouseTex);
		}
		else {
			houseSprite.setTexture(houseTex);
		}
	}
	public void incrementScore() {
		boxesHere++;
		update();
	}
	public void decrementScore() {
		boxesHere--;
		update();
	}
	@Override
	public void delete() {
		houseAt[address] = null;
		game.activeWorld.destroyBody(body);
	}
	private void addBoxesNeeded() {
		boxesNeeded++;
	}
	protected static int assignAddress() {
		Delivery game = Delivery.getGame();
		int address = game.random.nextInt(count) + 1;
		houseAt[address].addBoxesNeeded();
		return address;
	}
	public static void spawn(MapObjects mapObjects) {
		count = mapObjects.getCount();
		houseAt = new House[count + 1];
		for (int i = 0; i < count; i++) {
			MapObject mapObject = mapObjects.get(i);
			Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();
			houseAt[i + 1] = new House(rect.x / 4 + rect.width / 8, rect.y / 4, rect.width / 4, rect.height / 4, i + 1);
		}
	}
	public static void spawn(int houseCount) {
		count = houseCount;
		for (int i = 0; i < count; i++) {
			houseAt[i + 1] = new House(40 + 15 * i, 8, 10, 10, i + 1);
		}
	}
}
