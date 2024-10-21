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
	private final ApplicationType platform;
	
	public GameInputProcessor(ApplicationType platform) {
		this.platform = platform;
	}
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.W:
				Level.player.setPropelling(true);
				break;
			case Input.Keys.A:
				Level.player.setTiltLeft(true);
				break;
			case Input.Keys.D:
				Level.player.setTiltRight(true);
				break;
			case Input.Keys.S:
				Level.player.setClawing(true);
				break;
			case Input.Keys.L:
				Level.player.releaseGrip();
				break;
			case Input.Keys.K:
				Level.player.kill();
				Level.player = new Player();
				break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.W:
				Level.player.setPropelling(false);
				break;
			case Input.Keys.A:
				Level.player.setTiltLeft(false);
				break;
			case Input.Keys.D:
				Level.player.setTiltRight(false);
				break;
			case Input.Keys.S:
				Level.player.setClawing(false);
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
			Level.player.setPropelling(true);
			propellingPointer = pointer;
		}
		else if (!Level.player.getGripping()) {
			Level.player.setClawing(true);
			clawingPointer = pointer;
		}
		else {
			Level.player.releaseGrip();
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (platform == Application.ApplicationType.Desktop) {
			return true;
		}
		if (pointer == propellingPointer) {
			Level.player.setPropelling(false);
			propellingPointer = -1;
		}
		else if (pointer == clawingPointer) {
			Level.player.setClawing(false);
			clawingPointer = -1;
		}
		return true;
	}
}
