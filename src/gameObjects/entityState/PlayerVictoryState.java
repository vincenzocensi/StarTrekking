/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameObjects.entityState;

import gameObjects.Player;

/**
 *
 * @author Demetrio
 */
public class PlayerVictoryState extends PlayerState{

    public PlayerVictoryState(Player p){
        super(p);
    }
    
    @Override
    public void nextState(EntityState state) { 
        p.setState(state);
    }

    @Override
    public void set() {
        
    }
    
}