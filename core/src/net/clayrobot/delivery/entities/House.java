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
import net.clayrobot.delivery.Delivery;
import net.clayrobot.delivery.levels.Level;

public class House extends Entity {
	private final Body body;
	private Color color = Color.ORANGE;
	private final Texture houseTex = new Texture("house/house.png");
	private final Sprite houseSprite = new Sprite(houseTex);
	private final Sprite checkSprite = new Sprite(new Texture("house/check.png"));
	private final Texture greenHouseTex = new Texture("green_house2.png");
	private final Sprite addressSprite = new Sprite(new Texture("house/check.png"));
	private static int count = 0;
	public final int address;
	private static House[] houseAt;
	private int boxesHere = 0;
	private int boxesNeeded = 0;
	FixtureDef fixtureDef = new FixtureDef();
	private House(float x, float y, float width, float height, int address) {
		this.address = address;
		game.staticBodyDef.position.set(x, y + height / 2);
		body = Level.world.createBody(game.staticBodyDef);
		
		PolygonShape square = new PolygonShape();
		square.setAsBox(width / 2, height / 2);
		fixtureDef.shape = square;
		fixtureDef.isSensor = true;
		body.setUserData(this);
		body.createFixture(fixtureDef);
		width *= 1.2f;
		height *= 1.2f;
		houseSprite.setBounds(x - width / 2, y - 0.3f, width, height);
		
		
		addressSprite.setBounds(x - width / 2, y - 0.3f, width, height);
		checkSprite.setTexture(new Texture("house/" + address + ".png"));
		checkSprite.setBounds(x - width / 2, y - 0.3f, width, height);
		update();
	}
	
	@Override
	public void draw(float deltaTime) {
		houseSprite.draw(game.batch);
		//addressSprite.draw(game.batch);
		checkSprite.draw(game.batch);
		if (boxesHere >= boxesNeeded) {
			addressSprite.draw(game.batch);
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
	public void incrementScore() {
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
		checkSprite.getTexture().dispose();
		addressSprite.getTexture().dispose();
		houseAt[address] = null;
		Level.world.destroyBody(body);
	}
	protected static int assignAddress() {
		Delivery game = Delivery.getGame();
		int address = game.random.nextInt(count) + 1;
		houseAt[address].boxesNeeded++;
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
