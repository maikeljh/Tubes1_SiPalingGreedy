package Models;

import java.util.*;

public class GameState {

    public World world;
    public List<GameObject> gameObjects;
    public List<GameObject> playerGameObjects;

    public GameState() {
        this.world = new World();
        this.gameObjects = new ArrayList<GameObject>();
        this.playerGameObjects = new ArrayList<GameObject>();
    }

    public GameState(World world , List<GameObject> gameObjects, List<GameObject> playerGameObjects) {
        this.world = world;
        this.gameObjects = gameObjects;
        this.playerGameObjects = playerGameObjects;
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public List<GameObject> getGameObjects() {
        return this.gameObjects;
    }

    public void setGameObjects(List<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
    }

    public List<GameObject> getPlayerGameObjects() {
        return this.playerGameObjects;
    }

    public void setPlayerGameObjects(List<GameObject> playerGameObjects) {
        this.playerGameObjects = playerGameObjects;
    }

}
