package net.clayrobot.delivery.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.FreetypeInjector;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.inject.OnCompletion;
import net.clayrobot.delivery.Delivery;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig () {
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(true);
		config.padHorizontal = 0;
		config.padVertical = 0;
		config.useAccelerometer = false;
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener () {
        return new Delivery();
    }
		
	@Override
	public void onModuleLoad() {
		FreetypeInjector.inject(new OnCompletion() {
			public void run() {
				// Replace HtmlLauncher with the class name
				// If your class is called FooBar.java than the line should be FooBar.super.onModuleLoad();
				HtmlLauncher.super.onModuleLoad();
			}
		});
	}
}