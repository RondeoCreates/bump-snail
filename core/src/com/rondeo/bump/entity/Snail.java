package com.rondeo.bump.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.dongbat.jbump.CollisionFilter;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.Response;
import com.dongbat.jbump.World;
import com.dongbat.jbump.Response.Result;

public class Snail extends Actor implements Entity {
    World<Entity> world;
    Item<Entity> item;
    Rect rect;
    Result result;
    Vector2 targetPoint;
    final int speed = 40;

    CollisionFilter collisionFilter = new CollisionFilter() {
        @SuppressWarnings( "rawtypes" )
        @Override
        public Response filter( Item item, Item other ) {
            return Response.slide;
        }
    };

    public Snail( World<Entity> world, float x, float y, float width, float height, float targetX, float targetY ) {
        this.world = world;
        item = new Item<Entity>( this );
        world.add( item, x, y, width, height );
        setBounds( x, y, width, height );
        targetPoint = new Vector2( targetX, targetY );
    }

    @Override
    public void act(float delta) {
        result = world.move( item, getX() + targetPoint.x * (delta * speed), getY() + targetPoint.y * (delta * speed), collisionFilter );
        rect = world.getRect( item );
        setBounds( rect.x, rect.y, rect.w, rect.h );
    }

}
