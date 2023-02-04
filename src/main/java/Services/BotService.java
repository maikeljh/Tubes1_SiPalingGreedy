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
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD)
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

            var chosenArea = scanArea(foodList, playerList, obstacleList);
            foodList = chosenArea.get(0);
            playerList = chosenArea.get(1);
            obstacleList = chosenArea.get(2);

            /*if(obstacleList.size() > 0 && getDistanceBetween(this.bot, obstacleList.get(0)) < obstacleList.get(0).size + this.bot.size){
                if(this.bot.currentHeading - getHeadingBetween(obstacleList.get(0)) < 90){
                    playerAction.heading = this.bot.currentHeading + (90 - getHeadingBetween(obstacleList.get(0)));
                }
            }*/ if(foodList.size() == 0 && playerList.size() == 0 && obstacleList.size() == 0){
                playerAction.heading = getHeadingBetween(this.gameState.getWorld());
            } 
            else if(playerList.size() > 0 && playerList.get(0).size < this.bot.size){
                playerAction.heading = getHeadingBetween(playerList.get(0));
            } else if(foodList.size() > 0){
                /*if(getDistanceBetween(foodList.get(0), this.bot) == getDistanceBetween(foodList.get(1), this.bot)){
                    playerAction.heading = getHeadingBetween(foodList.get(2));
                } else {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                }*/
                playerAction.heading = getHeadingBetween(foodList.get(0));
            }
        }
        this.playerAction = playerAction;
    }

    public List<List<GameObject>> scanArea(List<GameObject> foodList, List<GameObject> playerList, List<GameObject> obstacleList){
        // Initialize area
        var area = 0;
        double maxScore = 0;
        List<GameObject> fixFoodList = new ArrayList<GameObject>();
        List<GameObject> fixPlayerList = new ArrayList<GameObject>();
        List<GameObject> fixObstacleList = new ArrayList<GameObject>();
        for(int i = 0; i < 12; i++){
            var headingFirst = i * 30;
            var headingLast = headingFirst + 30;
            var maxDistance = 250;
            double score = 0;
            List<GameObject> tempFoodList = new ArrayList<GameObject>();
            List<GameObject> tempPlayerList = new ArrayList<GameObject>();
            List<GameObject> tempObstacleList = new ArrayList<GameObject>();
            // Iterate Food List
            var idx = 0;
            while (idx < foodList.size() && getDistanceBetween(foodList.get(idx), this.bot) <= maxDistance){
                var head = getHeadingBetween(foodList.get(idx));
                if(head >= headingFirst && head <= headingLast){
                    if(foodList.get(idx).getGameObjectType() == ObjectTypes.FOOD){
                        score += (3.0 / getDistanceBetween(this.bot, foodList.get(idx)));
                    } else {
                        score += (6.0 / getDistanceBetween(this.bot, foodList.get(idx)));
                    }
                    tempFoodList.add(foodList.get(idx));
                }
                idx++;
            }
            // Iterate Player List
            idx = 0;
            var chooseToSkip = false;
            while (idx < playerList.size() && getDistanceBetween(playerList.get(idx), this.bot) <= maxDistance){
                var head = getHeadingBetween(playerList.get(idx));
                double theta = (double) playerList.get(idx).size / getDistanceBetween(playerList.get(idx), this.bot);
                theta = toDegrees(Math.asin(theta));
                var headFirstEnemy = head - theta;
                var headLastEnemy = head + theta;
                if((headingFirst >= headFirstEnemy && headingFirst <= headLastEnemy) || (headingLast >= headFirstEnemy && headingLast <= headLastEnemy) || (head >= headingFirst && head <= headingLast)){
                    if((double) playerList.get(idx).size * 6.0 / 5.0 <= this.bot.size){
                        score += ((double) playerList.get(idx).size / getDistanceBetween(this.bot, playerList.get(idx)));
                        System.out.print("Score musuh : ");
                        System.out.println(((double) playerList.get(idx).size / getDistanceBetween(this.bot, playerList.get(idx))));
                        tempPlayerList.add(playerList.get(idx));
                    } else {
                        System.out.print("SKIP Kegedean lol");
                        chooseToSkip = true;
                        break;
                    }
                }
                idx++;
            }
            if(chooseToSkip){
                break;
            }

            // Iterate Obstacle List
            idx = 0;
            chooseToSkip = false;
            while (idx < obstacleList.size() && getDistanceBetween(obstacleList.get(idx), this.bot) <= maxDistance){
                var head = getHeadingBetween(obstacleList.get(idx));
                if(head >= headingFirst && head <= headingLast){
                    /*if(obstacleList.get(idx).getGameObjectType() == ObjectTypes.GAS_CLOUD){
                        chooseToSkip = true;
                        break;
                    } else {*/
                        score -= (5 / getDistanceBetween(this.bot, obstacleList.get(idx)));
                        tempObstacleList.add(obstacleList.get(idx));
                }
                idx++;
            }
            if(chooseToSkip){
                break;
            }

            // Choose area
            if(score > maxScore){
                maxScore = score;
                area = i;
                fixFoodList = tempFoodList;
                fixPlayerList = tempPlayerList;
                fixObstacleList = tempObstacleList;
            }
        }
        System.out.print(this.gameState.getWorld().currentTick);
        System.out.print(" : ");
        System.out.println(maxScore);
        return Arrays.asList(fixFoodList, fixPlayerList, fixObstacleList);
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

    private int getHeadingBetween(World world) {
        var direction = toDegrees(Math.atan2(world.getCenterPoint().y - bot.getPosition().y,
                world.getCenterPoint().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
