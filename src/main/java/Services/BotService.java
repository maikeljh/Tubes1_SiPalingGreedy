package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!this.gameState.getGameObjects().isEmpty()) {
            var foodList = this.gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                        .comparing(item -> getDistanceBetween(this.bot, item)))
                    .collect(Collectors.toList());
            var playerList = this.gameState.getPlayerGameObjects()
                    .stream()
                    .filter(enemy -> enemy.id != this.bot.id)
                    .sorted(Comparator
                        .comparing(enemy -> getDistanceBetween(this.bot, enemy)))
                    .collect(Collectors.toList());
            var obstacleList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                    .sorted(Comparator
                        .comparing(obstacle -> getDistanceBetween(this.bot, obstacle)))
                    .collect(Collectors.toList());
            if(obstacleList.size() > 0 && getDistanceBetween(this.bot, obstacleList.get(0)) < obstacleList.get(0).size + this.bot.size){
                if(this.bot.currentHeading - getHeadingBetween(obstacleList.get(0)) < 90){
                    playerAction.heading = this.bot.currentHeading + (90 - getHeadingBetween(obstacleList.get(0)));
                }
            } else if(playerList.size() > 0 && playerList.get(0).size < this.bot.size + (1 / 4 * playerList.get(0).size)){
                playerAction.heading = getHeadingBetween(playerList.get(0));
            } else {
                if(getDistanceBetween(foodList.get(0), this.bot) == getDistanceBetween(foodList.get(1), this.bot)){
                    playerAction.heading = getHeadingBetween(foodList.get(2));
                } else {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                }
            }
        }
        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
