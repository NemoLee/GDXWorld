package com.blastedstudios.gdxworld.plugin.mode.light.typetable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.blastedstudios.gdxworld.plugin.mode.light.LightWindow;
import com.blastedstudios.gdxworld.world.light.DirectionalLight;
import com.blastedstudios.gdxworld.world.light.GDXLight;

public class DirectionalLightTable extends AbstractLightTable {
	private TextField directionField;
	
	public DirectionalLightTable(final Skin skin, LightWindow lightWindow, Color color, int rays, float direction){
		super(skin, lightWindow, color, rays);
		add(new Label("Type: Directional", skin));
		init();
		add(new Label("Direction: ", skin));
		add(directionField = new TextField(direction+"", skin)).width(AbstractLightTable.WIDTH);
		directionField.setMessageText("<direction>");
	}

	@Override public GDXLight create() {
		DirectionalLight light = new DirectionalLight();
		light.setDirection(Float.parseFloat(directionField.getText()));
		return super.create(light);
	}

	@Override public void setCoordinates(float x, float y) {}
}
