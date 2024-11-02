package net.clayrobot.delivery.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import net.clayrobot.delivery.AutobahnDelivery;
import java.util.UUID;
import net.clayrobot.delivery.levels.Level;

public class Box extends Entity {
	private final Body body;
	
	private final static Texture[] faces = {
		new Texture("box/smirk.png")
	};
	private final Sprite boxSprite = new Sprite(faces[0]);
	private final static Texture frameTex1 = new Texture("box/frame1.png");
	private final static Texture frameTex2 = new Texture("box/frame2.png");
	private final Sprite frameSprite = new Sprite(frameTex1);
	private Vector2 position;
	
	private static final float MIN_WIDTH = 1.2f;
	private static final float MIN_HEIGHT = MIN_WIDTH;
	private static final float MAX_WIDTH = 2.2f;
	private static final float MAX_HEIGHT = MAX_WIDTH;
	private static final float MAX_MASS = 0.75f;
	private static final float MIN_MASS = 0.3f;
	private final int INTERVAL = (int) (16 * (game.refreshRate / 60f));
	private int counter = game.random.nextInt(INTERVAL * 2);
	public final int address;
	private final float width, height;
	public boolean held = false;
	public final String uuid = UUID.randomUUID().toString();
	private final Sound fallSound = Gdx.audio.newSound(Gdx.files.internal("fallclean.wav"));
	public Box(float x, float y, float width, float height, float density, int address) {
		this.address = address;
		if (game.random.nextInt(2) == 1) boxSprite.flip(true, false);
		this.width = width;
		this.height = height;
		position = new Vector2(x, y);
		game.dynamicBodyDef.position.set(x, y);
		body = Level.world.createBody(game.dynamicBodyDef);
		body.setUserData(this);
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
	private Texture randomFace() {
		return faces[0];
	}
	public Body getBody() {
		return body;
	}
	@Override
	public void delete() {
		Level.world.destroyBody(body);
		fallSound.dispose();
	}
	protected static void dispose() {
		for (Texture texture : faces) {
			texture.dispose();
		}
		frameTex1.dispose();
		frameTex2.dispose();
	}
	boolean playSound = true;
	@Override
	protected void update() {
		position = body.getPosition();
		if (counter == 0) frameSprite.setTexture(frameTex1);
		else if (counter == INTERVAL) frameSprite.setTexture(frameTex2);
		counter++;
		if (counter >= INTERVAL * 2) counter = 0;
		if (body.getLinearVelocity().y < -2.5f && !held) {
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
		boxSprite.setBounds(position.x - boxSprite.getWidth() / 2, position.y - boxSprite.getHeight() / 2, width * 1.1f, height * 1.1f);
		boxSprite.setOriginCenter();
		boxSprite.setRotation((float) Math.toDegrees(body.getAngle()));
		boxSprite.draw(game.batch);
		
		frameSprite.setBounds(position.x - boxSprite.getWidth() / 2, position.y - boxSprite.getHeight() / 2, width * 1.1f, height * 1.1f);
		frameSprite.setOriginCenter();
		frameSprite.setRotation((float) Math.toDegrees(body.getAngle()));
		frameSprite.draw(game.batch);
		//if (drawDebug) font4.draw(batch, String.valueOf(address), position.x - 0.5f, position.y + 1);
	}
	public static void spawn(int spawnX, int spawnY, int amount) {
		final AutobahnDelivery game = AutobahnDelivery.getGame();
		float x, y;
		float width, height;
		float area;
		float density;
		float maxDensity, minDensity;
		for (int i = 0; i < amount; i++) {
			width = game.random.nextFloat() * (MAX_WIDTH - MIN_WIDTH) + MIN_WIDTH;
			height = game.random.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT) + MIN_HEIGHT;
			if (height > width) {
				float temp = height;
				height = width;
				width = temp;
			}
			area = width * height;
			maxDensity = MAX_MASS / area;
			minDensity = MIN_MASS / area;
			density = game.random.nextFloat() * (maxDensity - minDensity) + minDensity;
			x = game.random.nextFloat() * 14 + (spawnX - 7);
			y = (float) spawnY + height / 2f;
			new Box(x, y, width, height, density, House.assignAddress());
		}
	}
}