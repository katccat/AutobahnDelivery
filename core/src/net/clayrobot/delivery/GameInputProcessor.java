package net.clayrobot.delivery;

import net.clayrobot.delivery.levels.Level;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import net.clayrobot.delivery.entities.Player;

public class GameInputProcessor extends InputAdapter {
	private int propellingPointer = -1; // these ints keep track of touches that were intended as either propell or claw (in case finger is dragged elsewhere)
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
			return true; // disables touch on desktop (because mouse is treated same as touch and it's annoying)
		}
		if (screenX < Gdx.graphics.getWidth() / 2) { // if touch is on left side of screen
			level.getPlayer().setPropelling(true);
			propellingPointer = pointer; // keeps track of the pointer index that was used to propell
		}
		else if (!level.getPlayer().getGripping()) { // if touch is on right and player isn't already holding
			level.getPlayer().setClawing(true); // keeps track of the pointer index that was used to claw
			clawingPointer = pointer;
		}
		else {
			level.getPlayer().releaseGrip(); // touch was on right and player was already holding (player intended to release grip)
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (platform == Application.ApplicationType.Desktop) {
			return true;
		}
		if (pointer == propellingPointer) { // even if the pointer moved, it was kept track of so it is handled accordingly
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
