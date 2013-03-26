package com.blastedstudios.gdxworld.plugin.mode.background;

import java.util.Collections;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.gdxworld.math.PolygonUtils;
import com.blastedstudios.gdxworld.ui.leveleditor.mode.AbstractMode;
import com.blastedstudios.gdxworld.world.GDXBackground;
import com.blastedstudios.gdxworld.world.GDXLevel;

@PluginImplementation
public class BackgroundMode extends AbstractMode {
	private BackgroundWindow backgroundWindow;
	
	public void addBackground(GDXBackground background) {
		Gdx.app.log("BackgroundMouseMode.addBackground", background.toString());
		if(!screen.getLevel().getBackgrounds().contains(background)){
			screen.getLevel().getBackgrounds().add(background);
			Collections.sort(screen.getLevel().getBackgrounds());
		}
	}

	public void removeBackground(GDXBackground background) {
		Gdx.app.log("BackgroundMouseMode.removeBackground", background.toString());
		screen.getLevel().getBackgrounds().remove(background);
	}
	
	@Override public boolean touchDown(int x, int y, int x1, int y1) {
		super.touchDown(x,y,x1,y1);
		Gdx.app.debug("BackgroundMouseMode.touchDown", "x="+x+ " y="+y);
		GDXBackground background = getClosest(coordinates.x, coordinates.y);
		if(background == null)
			background = new GDXBackground();
		if(backgroundWindow == null)
			screen.getStage().addActor(backgroundWindow = new BackgroundWindow(screen.getSkin(), this, background));
		backgroundWindow.setCenter(new Vector2(coordinates.x, coordinates.y));
		return false;
	}
	
	private GDXBackground getClosest(float x, float y){
		for(GDXBackground background : screen.getLevel().getBackgrounds()){
			Texture tex = screen.getGDXRenderer().getTexture(background.getTexture());
			if(tex != null && PolygonUtils.aabbCollide(x,y,
				background.getCoordinates().x, background.getCoordinates().y,
				tex.getWidth()*background.getScale(), tex.getHeight()*background.getScale()))
				return background;
		}
		return null;
	}
	
	@Override public boolean contains(float x, float y) {
		return backgroundWindow != null && backgroundWindow.contains(x, y);
	}

	@Override public void clean() {
		if(backgroundWindow != null)
			backgroundWindow.remove();
		backgroundWindow = null;
	}

	@Override public void loadLevel(GDXLevel level) {
		super.loadLevel(level);
		for(GDXBackground background : level.getBackgrounds())
			addBackground(background);
	}
}