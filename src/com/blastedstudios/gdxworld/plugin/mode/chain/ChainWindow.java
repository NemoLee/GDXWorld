package com.blastedstudios.gdxworld.plugin.mode.chain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.gdxworld.plugin.mode.circle.CircleTable;
import com.blastedstudios.gdxworld.ui.AbstractWindow;
import com.blastedstudios.gdxworld.ui.leveleditor.VertexTable;
import com.blastedstudios.gdxworld.util.Properties;
import com.blastedstudios.gdxworld.world.GDXLevel;
import com.blastedstudios.gdxworld.world.joint.RevoluteJoint;
import com.blastedstudios.gdxworld.world.shape.GDXCircle;
import com.blastedstudios.gdxworld.world.shape.GDXPolygon;
import com.blastedstudios.gdxworld.world.shape.GDXShape;

public class ChainWindow extends AbstractWindow {
	private final VertexTable startTable, endTable;
	private final TextField distanceField;
	private final CircleTable circleTable;
	private final RectangleTable rectangleTable;
	private final Table shapeTable;
	private final CheckBox circleBox, rectangleBox;
	
	public ChainWindow(final Skin skin, final GDXLevel level) {
		super("Chain Editor", skin);
		startTable = new VertexTable(new Vector2(), skin, null);
		endTable = new VertexTable(new Vector2(), skin, null);
		distanceField = new TextField(Properties.get("level.chain.distance", "1"), skin);
		distanceField.setMessageText("<distance to next shape in chain");
		final Button createButton = new TextButton("Create", skin);
		createButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				createChain(level);
			}
		});
		
		circleBox = new CheckBox("Circle", skin);
		rectangleBox = new CheckBox("Rectangle", skin);
		rectangleBox.setChecked(true);
		circleBox.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				rectangleBox.setChecked(false);
				circleBox.setChecked(true);
				shapeTable.reset();
				shapeTable.add(circleTable);
			}
		});
		rectangleBox.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				circleBox.setChecked(false);
				rectangleBox.setChecked(true);
				shapeTable.reset();
				shapeTable.add(rectangleTable);
			}
		});
		shapeTable = new Table(skin);
		circleTable = new CircleTable(skin, new GDXCircle());
		rectangleTable = new RectangleTable(skin, new GDXPolygon());
		shapeTable.add(rectangleTable);
		
		Table extraTable = new Table();
		extraTable.add(new Label("Start: ", skin));
		extraTable.add(startTable);
		extraTable.row();
		extraTable.add(new Label("End: ", skin));
		extraTable.add(endTable);
		extraTable.row();
		extraTable.add(new Label("Frequency: ", skin));
		extraTable.add(distanceField);
		extraTable.row();
		extraTable.add(circleBox);
		extraTable.add(rectangleBox);
		add(extraTable);
		row();
		add(shapeTable).colspan(2);
		row();
		add(createButton);
		setMovable(false);
		pack();
	}

	public void render(float delta, Camera camera, ShapeRenderer renderer){
		renderer.setColor(Color.ORANGE);
		float radius = circleTable.getRadius();
		Vector2 dir = endTable.getVertex().cpy().sub(startTable.getVertex()).nor();
		for(float i=0; i<startTable.getVertex().dst(endTable.getVertex()); i+=parseFrequency()){
			Vector2 coordinates = startTable.getVertex().cpy().add(dir.cpy().scl(i));
			renderer.circle(coordinates.x, coordinates.y, radius);
		}
	}
	
	public void touched(float x, float y){
		if(startTable.isCursorActive())
			startTable.setVertex(x, y);
		if(endTable.isCursorActive())
			endTable.setVertex(x, y);
	}
	
	private float parseFrequency(){
		try{
			return Float.parseFloat(distanceField.getText());
		}catch(Exception e){
			return 1f;
		}
	}
	
	private void createChain(GDXLevel level){
		Vector2 dir = endTable.getVertex().cpy().sub(startTable.getVertex()).nor();
		GDXShape lastShape = null;
		for(float i=0; i<startTable.getVertex().dst(endTable.getVertex()); i+=parseFrequency()){
			Vector2 coordinates = startTable.getVertex().cpy().add(dir.cpy().scl(i));
			if(circleBox.isChecked()){
				GDXCircle circle = new GDXCircle();
				circle.setBodyType(BodyType.DynamicBody);
				circle.setCenter(coordinates);
				circle.setRadius(circleTable.getRadius());
				circleTable.apply(circle);
				level.getCircles().add(circle);
				if(lastShape != null)
					level.getJoints().add(attach(lastShape, circle));
				lastShape = circle;
			}else if(rectangleBox.isChecked()){
				GDXPolygon rectangle = new GDXPolygon();
				rectangle.setBodyType(BodyType.DynamicBody);
				rectangle.setCenter(coordinates);
				rectangleTable.apply(rectangle);
				level.getPolygons().add(rectangle);
				if(lastShape != null)
					level.getJoints().add(attach(lastShape, rectangle));
				lastShape = rectangle;
			}else
				Gdx.app.log("ChainWindow.createChain", "Neither rectangle nor circle box selected!");
		}
	}
	
	private static RevoluteJoint attach(GDXShape lastShape, GDXShape shape){
		RevoluteJoint joint = new RevoluteJoint();
		joint.setBodyA(lastShape.getName());
		joint.setBodyB(shape.getName());
		joint.setAnchor(shape.getCenter().cpy().add(lastShape.getCenter()).div(2));
		return joint;
	}
}
