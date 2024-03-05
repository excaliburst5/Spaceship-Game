package com.spaceship.game;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.spaceship.game.Spceship.GameState;

public class Spceship extends ApplicationAdapter {
	
	static final int WINDOW_WIDTH = 1024;
	static final int WINDOW_HEIGHT = 512;
	
	static final int VIRTUAL_WIDTH = 1024;
	static final int VIRTUAL_HEIGHT = 720;
	
	//private float dt; 
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	//Font
	private BitmapFont font_18;
	private BitmapFont font_32;
	
	//BackGround
	private Texture spacebackground;
	private int backgroundScroll = 0;
	final private int BACKGROUND_SCROLL_SPEED = 150;
	final private int BACKGROUND_LOOPING_POINT = 500;
	
	//Ship
	private Texture shipImage;
	private Rectangle ship;
	private float shipVelocityX = 100;
	private float shipVelocityY = 200;
	private float backWardThrust = 120;
	private float acceleration = 250;
	private float brakeTimer = 0;
	private boolean upwardThruster = false;
	private boolean downwardThruster = false;
	private float distanceTraveled = 0.0f;
	
	//Asteroid
	private Texture asteroidImage;
	private Array<Rectangle> asteroids;
	private float lastAsteroidSpawn;
	
	private int score;
	
	GameState gameState = GameState.Start;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		
		font_18 = new BitmapFont(Gdx.files.internal("fonts/EBDragon18/EightBitDragon-18.fnt"), Gdx.files.internal("fonts/EBDragon18/EightBitDragon-18.png"),false);
		font_32 = new BitmapFont(Gdx.files.internal("fonts/EBDragon32/EightBitDragon-32.fnt"), Gdx.files.internal("fonts/EBDragon32/EightBitDragon-32.png"), false);
	
		ship = new Rectangle();
		ship.x = 50; 
		ship.y = 512 / 2 - 64 / 2 ; 
		ship.width = 64;
		ship.height = 64;
		
		asteroids = new Array<Rectangle>();
		spawnAsteroids();
	}

	@Override
	public void render () { 
		spacebackground = new Texture("space.png");
		shipImage = new Texture("ship.png");
		asteroidImage = new Texture("rocks/rock2.png");
		//update(Gdx.graphics.getDeltaTime());
		//bgDraw();
		updateGame();
		
		//Screen boundaries
		if(ship.y < 0) ship.y = 0;
		if(ship.y > 512 - 64) ship.y = 512 - 64;
		if(ship.x < 0) ship.x = 0;
		if(ship.x > 1024 - 64) ship.x = 1024 - 64; 
	}
	
	public void updateGame() {
		
		if(gameState == GameState.Start) {
			batch.begin();
			batch.draw(spacebackground, 0, -105, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
			font_32.draw(batch, "Spaceship", 400,280);
			font_18.draw(batch, "Press [SPACE] to start" ,370, 210);
			batch.end();
		}
		if(gameState == GameState.Playing) {
			draw();
		}
		if(gameState == GameState.Gameover) {
			resetWorld();
			batch.begin();
			batch.draw(spacebackground, 0, -105, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
			font_32.draw(batch, "Game Over! Your Score " + (int)(Math.round(distanceTraveled)), 250,280);
			font_18.draw(batch, "Press [SPACE] to start" ,370, 210);
			batch.end();
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) gameState = GameState.Playing;
		
		//ship Controlls
		shipMovement();
		if(upwardThruster) {
			ship.y += shipVelocityY * Gdx.graphics.getDeltaTime(); 
			ship.x += shipVelocityX * Gdx.graphics.getDeltaTime();
		}
		if(downwardThruster){
			ship.y -= shipVelocityY * Gdx.graphics.getDeltaTime(); 
			ship.x += shipVelocityX * Gdx.graphics.getDeltaTime();
		}
		
		moveAsteroid();
	}
	
	public void draw() {
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getDistanceTraveled(Gdx.graphics.getDeltaTime());
		update(Gdx.graphics.getDeltaTime());
		batch.begin();
		batch.draw(spacebackground, -backgroundScroll, -105, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		batch.draw(spacebackground, VIRTUAL_WIDTH - backgroundScroll, -105, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);//this makes the moving effect of the bg
		batch.draw(shipImage, ship.x, ship.y);
		for(Rectangle asteroid: asteroids) {//drawing the asteroids that is stored in the Array<Rectangle>
			batch.draw(asteroidImage, asteroid.x, asteroid.y);
		}
		batch.end();
	}
	
	public void update(float dt) {
		//scroll the background image
		backgroundScroll += BACKGROUND_SCROLL_SPEED * dt;
		//System.out.println(backgroundScroll);
		//Reset the background image if it reach the loop point
		if(backgroundScroll > BACKGROUND_LOOPING_POINT) {
			backgroundScroll = 0;
		}
	}
	
	public void shipMovement() {
		/*if W is pressed and the thrusters for downward motion is off then turn 
		on the thruster upward on Same for S
		*/
		if(Gdx.input.isKeyPressed(Input.Keys.W))if(!downwardThruster)upwardThruster = true;
		
		if(Gdx.input.isKeyPressed(Input.Keys.S))if(!upwardThruster)downwardThruster = true;
		
		//for backward motion
		if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SPACE)) ship.x -= backWardThrust * Gdx.graphics.getDeltaTime();
		
		/*Brake, braketimer keeps track of the how long SPACE is pressed then when time requirement meet
		 * then turn off upward and downward thruster. Else set braketimer back to 0
		 */
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			brakeTimer += Gdx.graphics.getDeltaTime();
			
			if(brakeTimer > 0.2f) {
				if(upwardThruster)upwardThruster = false;
				if(downwardThruster)downwardThruster = false;
			}
		}else brakeTimer = 0.0f;

	}
	
	public void spawnAsteroids() {
		Rectangle asteroid = new Rectangle();
		asteroid.x = WINDOW_WIDTH;
		asteroid.y = MathUtils.random(0,WINDOW_HEIGHT-64); //spawn at the right side of the screen
		asteroid.width = 32;
		asteroid.height = 32;
		asteroids.add(asteroid);
		lastAsteroidSpawn = TimeUtils.nanoTime();
	}
	
	public void moveAsteroid() {
		if(TimeUtils.nanoTime() - lastAsteroidSpawn > 1000000000) spawnAsteroids();
		
		for(Iterator<Rectangle> iter = asteroids.iterator(); iter.hasNext();) {
			Rectangle asteroid = iter.next();
			asteroid.x -= 100 * Gdx.graphics.getDeltaTime();
			if(asteroid.x + 64 < 0) iter.remove();//if asteroid reach x = 0 then remove them
			
			if(asteroid.overlaps(ship)) {//if hit set Gamestate to gameover
				gameState = GameState.Gameover;
			}
		}
	}
	
	public void getDistanceTraveled(float dt) {
		distanceTraveled += 10.0f * dt; 
		System.out.println(distanceTraveled);
		//distanceLabel.setText("Distance Traveled: " + (int)(Math.round(distanceTraveled)));
		if(gameState == GameState.Gameover) {
			score = (int)(Math.round(distanceTraveled));
		}
	}
	
	private void resetWorld() {
		//reset ship location
		ship.x = 50; 
		ship.y = 512 / 2 - 64 / 2 ; 
		
		//off the thruster
		upwardThruster = false;
		downwardThruster = false;
		
		asteroids.clear();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		spacebackground.dispose();
		font_18.dispose();
		font_32.dispose();
		shipImage.dispose();
		asteroidImage.dispose();
	}
	
	static enum GameState {
		Start, Playing, Gameover
	}
}
