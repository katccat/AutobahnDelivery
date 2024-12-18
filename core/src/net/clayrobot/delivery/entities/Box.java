package net.clayrobot.delivery.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import net.clayrobot.delivery.AutobahnDelivery;
import java.util.UUID;
import net.clayrobot.delivery.levels.Level;

public class Box extends Entity {
	private final Body body;
	
	private final Sprite boxSprite = new Sprite(new Texture("box/box3dback.png"));
	private final Texture[] boxTextures = {
		new Texture("box/box3dback.png"), // only "back" is used right now
		new Texture("box/box3dfront.png")
	};
	private final Texture[] faceTexture;
	//private final Texture outlineTexture1 = new Texture("box/frame1.png");
	//private final Texture outlineTexture2 = new Texture("box/frame2.png");
	private Vector2 position;
	
	private static final float MIN_WIDTH = 1.2f;
	private static final float MIN_HEIGHT = MIN_WIDTH; // min width and height are both 1.2 units
	private static final float MAX_WIDTH = 2.2f;
	private static final float MAX_HEIGHT = MAX_WIDTH; // max width and height are both 2.2 units
	private static final float MAX_MASS = 0.75f;
	private static final float MIN_MASS = 0.3f;
	private final int INTERVAL = (int) (16 * (game.refreshRate / 60f));
	private int counter = game.random.nextInt(INTERVAL * 2); // so the boxes don't all start on the same frame
	public final int address;
	private final float width, height;
	public boolean held = false; // boolean used to play fall sound (when box is both falling and not held)
	private final static Array<Box> boxes = new Array<>();
	public final String uuid = UUID.randomUUID().toString(); // used to keep track of boxes (used by contact listener)
	private final Sound fallSound = Gdx.audio.newSound(Gdx.files.internal("fallclean.wav")); // slide whistle falling sound
	public Box(float x, float y, float width, float height, float density, int address) {
		boxes.add(this);
		this.address = address;
		if (game.random.nextInt(2) == 1) boxSprite.flip(true, false);
		String[] faceNames = { // none of the stuff about face textures has not been re-implemented yet
			"smirk",
			"happy",
			"content",
			"o"
		};
		String faceName = faceNames[game.random.nextInt(faceNames.length)];
		faceTexture = new Texture[4];
		
		this.width = width;
		this.height = height;
		position = new Vector2(x, y);
		game.dynamicBodyDef.position.set(x, y);
		body = Level.world.createBody(game.dynamicBodyDef);
		body.setUserData(this); // this is so the box2d contact listener can get a reference to the object from the body
		PolygonShape square = new PolygonShape();
		square.setAsBox(this.width / 2, this.height / 2);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = square;
		fixtureDef.density = density;
		fixtureDef.friction = 0.78f;
		fixtureDef.restitution = 0.22f;

// Create our fixture and attach it to the body
		body.createFixture(fixtureDef);
		square.dispose();
	}
	private Texture randomFace() { // not currently used
		return faceTexture[game.random.nextInt(faceTexture.length)];
	}
	public Body getBody() {
		return body;
	}
	protected static void clear() {
		boxes.clear();
	}
	@Override
	public void delete() {
		Level.world.destroyBody(body);
		fallSound.dispose();
		for (Texture texture : boxTextures) {
			texture.dispose();
		}
	}
	protected static void dispose() {

	}
	boolean playSound = true;
	@Override
	protected void update() {
		position = body.getPosition();
		//if (counter == 0) frameSprite.setTexture(outlineTexture1);
		//else if (counter == INTERVAL) frameSprite.setTexture(outlineTexture2);
		counter++;
		if (counter >= INTERVAL * 2) counter = 0;
		if (body.getLinearVelocity().y < -2.5f && !held) { // play slide whistle if y velocity less than 2.5 and not being held
			if (playSound) fallSound.play();
			playSound = false;
		}
		else {
			fallSound.stop();
			playSound = true;
		}
	}
	@Override
	public void draw(float deltaTime) {
		super.draw(deltaTime);
		//boxSprite.setTexture(boxTextures[1]);
		boxSprite.setBounds(position.x - boxSprite.getWidth() / 2, position.y - boxSprite.getHeight() / 2, width * 1.25f, height * 1.25f); // move sprite to location of physics object
		boxSprite.setOriginCenter();
		boxSprite.setRotation((float) Math.toDegrees(body.getAngle())); // rotate sprite to match rotation of physics object
		boxSprite.draw(game.batch);
		
		//frameSprite.setBounds(position.x - boxSprite.getWidth() / 2, position.y - boxSprite.getHeight() / 2, width * 1.1f, height * 1.1f);
		//frameSprite.setOriginCenter();
		//frameSprite.setRotation((float) Math.toDegrees(body.getAngle()));
		//frameSprite.draw(game.batch);
		//if (drawDebug) font4.draw(batch, String.valueOf(address), position.x - 0.5f, position.y + 1);
	}
	private void drawSides() { // not used
		
		update();
		boxSprite.setTexture(boxTextures[0]);
		boxSprite.setBounds(position.x - boxSprite.getWidth() / 2, position.y - boxSprite.getHeight() / 2, width * 1.2f, height * 1.2f);
		boxSprite.setOriginCenter();
		boxSprite.setRotation((float) Math.toDegrees(body.getAngle()));
		boxSprite.draw(game.batch);
	}
	public static void drawAll() {
		//for (Box box : boxes) {
		//	box.drawSides();
		//}
	}
	public static void spawn(int spawnX, int spawnY, int amount) { // static spawn method that spawns boxes in a pile and gives them addresses
		final AutobahnDelivery game = AutobahnDelivery.getGame();
		float x, y;
		float width, height;
		float area;
		float density;
		float maxDensity, minDensity;
		for (int i = 0; i < amount; i++) {
			width = game.random.nextFloat() * (MAX_WIDTH - MIN_WIDTH) + MIN_WIDTH;
			height = game.random.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT) + MIN_HEIGHT;
			if (height > width) { // if height is greater than width, swap them (I don't want tall boxes)
				float temp = height;
				height = width;
				width = temp;
			}
			area = width * height; // area is used to compute min amd max densities based on max mass constant (so boxes aren't too heavy or light)
			maxDensity = MAX_MASS / area;
			minDensity = MIN_MASS / area;
			density = game.random.nextFloat() * (maxDensity - minDensity) + minDensity;
			x = game.random.nextFloat() * 14 + (spawnX - 7);
			y = (float) spawnY + height / 2f;
			new Box(x, y, width, height, density, House.assignAddress());
		}
	}
}