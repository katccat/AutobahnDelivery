package net.clayrobot.delivery.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.ScreenUtils;
import net.clayrobot.delivery.AutobahnDelivery;
import net.clayrobot.delivery.entities.Box;
import net.clayrobot.delivery.entities.Entities;
import net.clayrobot.delivery.entities.House;
import net.clayrobot.delivery.entities.Player;

public class TestGrounds extends Level {
	private final int GROUND_HEIGHT = 8;
	private final int winningScore = 6;
	private Sprite groundSprite;
	private Body groundBody;
	public TestGrounds(AutobahnDelivery game) {
		super(game, 200, 120);
		groundSprite = new Sprite(new Texture("levels/void/grass.jpg"));
		groundSprite.setBounds(0, 0, WORLD_WIDTH, GROUND_HEIGHT);
		game.staticBodyDef.position.set(WORLD_WIDTH / 2, GROUND_HEIGHT / 2);
		groundBody = world.createBody(game.staticBodyDef);
		PolygonShape square = new PolygonShape();
		square.setAsBox(WORLD_WIDTH / 2, GROUND_HEIGHT / 2);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = square;
		fixtureDef.friction = 0.9f;
		fixtureDef.restitution = 0.3f;
		groundBody.createFixture(fixtureDef);
		start();
	}
	@Override
	public void start() {
		super.start();
		House.spawn(4);
		Player.spawn.x = 25;
		Player.spawn.y = GROUND_HEIGHT + 5;
		Player.EnabledArmType = Player.ArmType.CLAW1;
		player = new Player();
		Box.spawn(15, GROUND_HEIGHT, winningScore);
	}
	@Override
	public void render(float deltaTime) {
		ScreenUtils.clear(1, 1, 1, 1);
		super.render(deltaTime);
		game.batch.begin();
		Entities.draw(deltaTime);
		groundSprite.draw(game.batch);
		game.batch.end();
		if (game.drawDebug) game.debugRenderer.render(world, camera.combined);
	}
	@Override
	public void dispose() {
		super.dispose();
		groundSprite.getTexture().dispose();
	}
}
