package entity;

import graphics.EntitySprite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import frames.GameFrame;
import frames.GamePanel;
import tiles.TileFacade;
import util.AABB;
import util.KeyHandler;
import util.Position;
import util.EntityState;

public class Player extends Entity implements Observer {

    private final int MAXHEALTHPOINTS = 3;
    private final float H = 100;
    private final float DIST = 150;
    private int hp;
    private float vx2;
    
    private KeyHandler khdl;
    int action;
    private ArrayList<Shot> shots = new ArrayList<Shot>();
    
    DecimalFormat df = new DecimalFormat();
    private boolean invincible;
    private float invStartTime;
    private boolean visible;
    private boolean falling = false;
    
    float instantVx = 0;
    
    boolean changeMotion = false;

    public Player(EntitySprite sprite, Position origin, int size, KeyHandler khdl) {
        super(sprite, origin, size, EntityState.RUN);
        this.hp = MAXHEALTHPOINTS;
        this.bounds = new AABB(pos, 16, 32, 40, 32);
        this.khdl = khdl;
        df.setMaximumFractionDigits(2);
        this.initialSpeed = 0.3f;
        this.vx = initialSpeed;
        this.acc = 0.00003f;
        this.visible = true;
        this.invincible = false;
        
    }

    public void move() {
        //PLAYER HORIZONTAL MOTION
        
        /*
        The following 2 if statements determine the motion of the player, based on his instant speed.
        If it reaches the max speed, the motion change from linear accelerated motion to linear motion, so the player doesn't increase his speed anymore.
        */
        if(instantVx < maxSpeed){
            instantVx = vx + acc*(timex);
        }
        if(instantVx > maxSpeed -0.001 && instantVx < maxSpeed +0.001 && changeMotion == false){
            changeMotion = true;
            changeMotion();
        }
        
        /*
        Equation of the linear accelerated motion on the horizontal axis.
        */
        dx = (float)((0.5*acc*Math.pow(timex, 2) + vx*timex)) + dx0;
        
        /*
        Collision detection: if the player touches a solid tile during his motion he will stop moving.
        */
        if(tc.collisionTile(dx-previousX, 0)){
            dx = previousX;
        } else {
            if (state != EntityState.DEAD) {
                timex += 1f;
            }
            previousX = dx;
        }

        //PLAYER VERTICAL MOTION
        
        /*
        Computation of the gravity and of the vertical speed in order to let the player make a jump of constant distance and height, regardless of the horizontal speed.
        When the player isn't in the jump state anymore the standard values of gravity and vertical speed are reset.
        */
        if(state == EntityState.JUMP){
            if(timey == 0){
                if(!falling) vy = -(float)((4*H*instantVx)/DIST);
                gravity = -(float)((H*8*Math.pow(instantVx, 2))/Math.pow(DIST, 2));
            }
            if (ani.playingLastFrame()) {
                ani.setDelay(-1);
            }
        } else {
            vy = 0;
            gravity = -0.01f;
        }
        
        /*
        Equation of the linear accelerated motion on the vertical axis.
        */
        dy = (float)((-0.5*gravity*Math.pow(timey, 2)+vy*timey)+dy0);
        
        /*
        Collision detection: if the player is on the ground he will not fall, if he is jumping and he touches a solid tile above is head he will start to fall..
        */
        if(tc.collisionTileDown(0, dy-previousY)){
            //System.err.println("collision down");
            dy = previousY;
            timey = 0;
            dy0 = dy;
            falling = false;
            if(state == EntityState.JUMP) state = EntityState.RUN;
        }else if(tc.collisionTileUp(0, dy-previousY)){
            dy = previousY;
            dy0 = previousY;
            vy = 0;
            timey = 0;
            falling = true;
        } else {
            timey += 1f;
            previousY = dy;
        }

        /*
        When the player change his state into attack state, he will perform all the attack animation and then he will fire a shot.
        */
        if (state == EntityState.ATTACK) {
            if (ani.playingLastFrame()) {
                attack();
                state = EntityState.RUN;
            }
        }

        /*
        When the player change his state into dead state, he will perform all the dead animation and then he will set himself to dead.
        */
        if (state == EntityState.DEAD) {
            isDead();
        }
        
        /*
        When the player change his state into crouch state, he will remain in the last animation frame till his state changes.
        */
        if (state == EntityState.CROUCH) {
            if (ani.playingLastFrame()) {
                ani.setDelay(-1);
            }
        }

    }
    
    public void changeMotion(){
        dx0 = previousX;
        timex = 0;
        acc = 0;
        vx = maxSpeed;
    }
    
    public void isDead(){
        if(ani.playingLastFrame()){
            dead = true;
        }
    }

    public void hitted() {
        invincible = true;
        visible = false;
        invStartTime = System.nanoTime();
        if(--hp==0){
            isDead();
            state = EntityState.DEAD;
        }
    }

    private void attack() {
        shots.add(new Shot(new EntitySprite("entity/shot", 32, 32), new Position(dx - 15, pos.getY() + 24), 48, vx + acc * (timex)));
    }
    
    public ArrayList<Shot> getShots(){
        return shots;
    }

    public void deleteShot(Shot s) {
        shots.remove(s);
    }
    
    public void updateGame(){
        super.updateGame(state);
        if(invincible){
            if(System.nanoTime()%9000 < 100 || System.nanoTime()%9000 > 100) visible = !visible;
            if(System.nanoTime() - invStartTime>= GamePanel.unitTime){
                invincible = false;
                visible = true;
            }
        }
        move();
        pos.setX(dx);    //update x position
        if (GamePanel.getMapPos().getX() + GameFrame.WIDTH < TileFacade.mapWidth * 16) {
            GamePanel.getMapPos().setX(dx);
        }
        pos.setY(dy);
        if(!shots.isEmpty()){
            for(int i=0; i<shots.size(); i++){
                if(shots.get(i).pos.getWorldVar().getX() - pos.getWorldVar().getX() > GameFrame.WIDTH || shots.get(i).collides()){
                    deleteShot(shots.get(i));
                } else {
                    shots.get(i).updateGame();
                }
            }
        }
        if(pos.getY() > GameFrame.HEIGHT){
                dead = true;  
        }
    }

    @Override
    public void render(Graphics2D g) {  //draw the player in the panel
        if (visible == true) {
            g.drawImage(ani.getImage(), (int) pos.getWorldVar().getX(), (int) pos.getWorldVar().getY(), size, size, null);
//            g.setColor(Color.blue);
//            g.drawRect((int) (pos.getWorldVar().getX() + bounds.getXOffset()), (int) (pos.getWorldVar().getY() + bounds.getYOffset()), (int) bounds.getWidth(), (int) bounds.getHeight());
        }

        if (!shots.isEmpty()) {
            for (int i = 0; i < shots.size(); i++) {
                shots.get(i).render(g);
            }
        }
    }

    private void mapValueAction(int key, boolean b) {
        if (true) { //in case the player is alive
            if ((key == 4) && state == EntityState.RUN && tc.collisionTileDown(0, 1)) {
                state = EntityState.JUMP;
                timey = 0;
            }
            if (key == 3 && currentState == EntityState.RUN) {
                state = EntityState.ATTACK;
            }
            if(key == 5 && (state == EntityState.RUN || state == EntityState.CROUCH)){
                state = EntityState.CROUCH;
                
                this.bounds.setBox(16, 12, 40, 52);
                if(!b){
                    this.bounds.setBox(16, 32, 40, 32);
                    state = EntityState.RUN;
                }
            } 
            
        } else {
            state = EntityState.DEAD;
        }

    }

    @Override
    public void update(Observable o, Object s) {
        if (o == this.khdl) {
            int key = this.khdl.getValue();
            boolean b = khdl.isPressed();
            mapValueAction(key, b);
        }
    }

    public int getHP() {
        return this.hp;
    }
}
