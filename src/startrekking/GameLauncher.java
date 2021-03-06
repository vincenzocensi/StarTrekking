/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package startrekking;

import gamestate.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author Star Trekking
 */
/**
 * *
 * Class that has the goal of launch the main menu.
 */
public class GameLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new GameStateManager();
                } catch (IOException ex) {
                    Logger.getLogger(GameLauncher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
