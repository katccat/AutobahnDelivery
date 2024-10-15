package net.clayrobot.delivery.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Billboard extends Entity {
	private final static float FRAME_SCALE = 1.06f;
	private final Sprite billboardSprite;
	private final Sprite frameSprite = new Sprite(new Texture("propaganda/frame2.png"));
	private final Texture texture1;
	private Texture texture2;
	private boolean animated = false;
	private int interval;
	private int counter = 0;
	public Billboard(int x, int y, float width, float height, Texture texture) {
		texture1 = texture;
		billboardSprite = new Sprite(texture1);
		billboardSprite.setBounds(x - width / 2, y - height / 2, width, height);
		frameSprite.setBounds(x - width * FRAME_SCALE / 2, y - height * FRAME_SCALE / 2, width * FRAME_SCALE, height * FRAME_SCALE);
	}
	public Billboard(int x, int y, float width, float height, int interval, Texture... texture) {
		this(x, y, width, height, texture[0]);
		texture2 = texture[1];
		animated = true;
		this.interval = (int) (interval * (game.refreshRate / 60f));
	}
	public Billboard(int x, int y, float width, float height, Texture... texture) {
		this(x, y, width, height, 28, texture);
	}
	
	@Override
	public void draw(float deltaTime) {
		if (deltaTime > 0 && animated) update();
		frameSprite.draw(game.batch);
		billboardSprite.draw(game.batch);
		
		
	}
	@Override
	protected void update() {
		if (counter == 0) {
			billboardSprite.setTexture(texture1);
		} else if (counter == interval) {
			billboardSprite.setTexture(texture2);
		}
		counter++;
		if (counter >= interval * 2)
			counter = 0;
	}
	@Override
	public void delete() {
		texture1.dispose();
		texture2.dispose();
		frameSprite.getTexture().dispose();
	}
}
