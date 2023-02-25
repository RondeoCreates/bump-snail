package com.rondeo.bump;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.ScreenUtils;

public class BumpSnail extends Game {
	
	@Override
	public void create () {
		setScreen( new GameScreen() );
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
