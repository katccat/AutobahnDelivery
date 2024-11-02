package net.clayrobot.delivery;

import net.clayrobot.delivery.levels.Level;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import net.clayrobot.delivery.entities.Player;

public class GameInputProcessor extends InputAdapter {
	private int propellingPointer = -1;
	private int clawingPointer = -1;
	private final Level level;
	private final ApplicationType platform;
	
	public GameInputProcessor(Level level) {
		this.level = level;
		this.platform = level.game.platform;
	}
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.W:
				level.getPlayer().setPropelling(true);
				break;
			case Input.Keys.A:
				level.getPlayer().setTiltLeft(true);
				break;
			case Input.Keys.D:
				level.getPlayer().setTiltRight(true);
				break;
			case Input.Keys.S:
				level.getPlayer().setClawing(true);
				break;
			case Input.Keys.L:
				level.getPlayer().releaseGrip();
				break;
			case Input.Keys.K:
				level.getPlayer().kill();
				level.setPlayer(new Player());
				break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.W:
				level.getPlayer().setPropelling(false);
				break;
			case Input.Keys.A:
				level.getPlayer().setTiltLeft(false);
				break;
			case Input.Keys.D:
				level.getPlayer().setTiltRight(false);
				break;
			case Input.Keys.S:
				level.getPlayer().setClawing(false);
				break;
		}
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (platform == Application.ApplicationType.Desktop) {
			return true;
		}
		if (screenX < Gdx.graphics.getWidth() / 2) {
			level.getPlayer().setPropelling(true);
			propellingPointer = pointer;
		}
		else if (!level.getPlayer().getGripping()) {
			level.getPlayer().setClawing(true);
			clawingPointer = pointer;
		}
		else {
			level.getPlayer().releaseGrip();
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (platform == Application.ApplicationType.Desktop) {
			return true;
		}
		if (pointer == propellingPointer) {
			level.getPlayer().setPropelling(false);
			propellingPointer = -1;
		}
		else if (pointer == clawingPointer) {
			level.getPlayer().setClawing(false);
			clawingPointer = -1;
		}
		return true;
	}
}
