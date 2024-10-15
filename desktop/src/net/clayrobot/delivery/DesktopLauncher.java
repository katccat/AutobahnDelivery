package net.clayrobot.delivery;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(800, 480);
		
		config.setForegroundFPS(60);
		config.setInitialBackgroundColor(Color.WHITE);
		config.setHdpiMode(HdpiMode.Pixels);
		config.setTitle("Autobahn Delivery Inc.");
		config.setWindowSizeLimits(500, 300, -1, -1);
		config.setWindowIcon(FileType.Internal, "icon.png");
		config.disableAudio(false);
		new Lwjgl3Application(new Delivery(), config);
	}
}
