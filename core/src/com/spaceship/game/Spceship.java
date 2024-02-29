package com.spaceship.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Spceship extends ApplicationAdapter {
	
	static final int WINDOW_WIDTH = 1024;
	static final int WINDOW_HEIGHT = 512;
	
	static final int VIRTUAL_WIDTH = 1024;
	static final int VIRTUAL_HEIGHT = 720;
	
	//private float dt; 
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	private Texture spacebackground;
	private int backgroundScroll = 0;
	
	final private int BACKGROUND_SCROLL_SPEED = 300;
	
	final private int BACKGROUND_LOOPING_POINT = 600;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
	}

	@Override
	public void render () { 
		spacebackground = new Texture("space.png");
		
		update(Gdx.graphics.getDeltaTime());
		bgDraw();
	}
	
	public void bgDraw() {
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(spacebackground, -backgroundScroll, -105, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		batch.draw(spacebackground, VIRTUAL_WIDTH - backgroundScroll, -105, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		batch.end();
	}
	
	public void update(float dt) {
		backgroundScroll += BACKGROUND_SCROLL_SPEED * dt;
		System.out.println(backgroundScroll);
		if(backgroundScroll > BACKGROUND_LOOPING_POINT) {
			backgroundScroll = 0;
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		spacebackground.dispose();
	}
	
	static enum gameState {
		Start, Playing, Gameover, Pause
	}
}
