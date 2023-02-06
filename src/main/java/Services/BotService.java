package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private boolean shootTorpedo;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        this.shootTorpedo = false;
    }

    public void setShootTorpedo(boolean shoot){
        this.shootTorpedo = shoot;
    }

    public boolean getShootTorpedo(){
        return this.shootTorpedo;
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
        // Initialize action and heading
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);
        // While game objects not empty
        if (!this.gameState.getGameObjects().isEmpty()) {
            // Getting food list
            var foodList = this.gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                    .sorted(Comparator
                        .comparing(item -> getDistanceBetween(this.bot, item)))
                    .collect(Collectors.toList());
            // Getting food list
            var foodNotNearEdgeList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD) && 
                    (this.gameState.getWorld().radius - getDistanceBetween(item, this.gameState.getWorld()) >= 1.2 * (double) this.bot.size))
                    .sorted(Comparator
                        .comparing(item -> getDistanceBetween(this.bot, item)))
                    .collect(Collectors.toList());
            // Getting player list
            var playerList = this.gameState.getPlayerGameObjects()
                    .stream()
                    .filter(enemy -> enemy.id != this.bot.id)
                    .sorted(Comparator
                        .comparing(enemy -> getDistanceBetween(this.bot, enemy)))
                    .collect(Collectors.toList());
            // Getting enemy list to shoot
            var musuhList = this.gameState.getPlayerGameObjects()
                    .stream()
                    .filter(enemy -> enemy.id != this.bot.id)
                    .sorted(Comparator
                        .comparing(enemy -> getDistanceBetween(this.bot, enemy)))
                    .collect(Collectors.toList());
            // Getting obstacle list to avoid
            var obstacleList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                    .sorted(Comparator
                        .comparing(obstacle -> getDistanceBetween(this.bot, obstacle)))
                    .collect(Collectors.toList());

            // Choosing best area
            var chosenArea = scanArea(foodList, playerList, obstacleList);
            foodList = chosenArea.get(0);
            playerList = chosenArea.get(1);
            obstacleList = chosenArea.get(2);

            // If there is no food and enemy nearby
            if(foodList.size() == 0 && playerList.size() == 0){
                if(!this.shootTorpedo && musuhList.size() > 0 && getDistanceBetween(musuhList.get(0), this.bot) - musuhList.get(0).size - this.bot.size <= this.bot.size + 150){
                    playerAction.heading = getHeadingBetween(musuhList.get(0));
                    var nabrak = false;
                    for(int x = 0; x < foodList.size(); x++){
                        if(playerAction.heading == getHeadingBetween(foodList.get(x))){
                            nabrak = true;
                        }
                    }
                    if(!nabrak){
                        System.out.println("Nembak gan");
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        this.shootTorpedo = true;
                    } else {
                        System.out.println("Ke tengah bang");
                        playerAction.heading = getHeadingBetween(this.gameState.getWorld());
                        this.shootTorpedo = false;
                    }
                } else {
                    if(foodNotNearEdgeList.size() > 0){
                        playerAction.heading = getHeadingBetween(foodNotNearEdgeList.get(0));
                        System.out.println("Cari makan yang gak di ujung");
                    } else {
                        System.out.println("Ke tengah bang");
                        playerAction.heading = getHeadingBetween(this.gameState.getWorld());
                    }
                    this.shootTorpedo = false;
                }
            } else if(playerList.size() > 0 && playerList.get(0).size < this.bot.size){
                // If there is a smaller enemy nearby
                playerAction.heading = getHeadingBetween(playerList.get(0));
                var nabrak = false;
                for(int x = 0; x < foodList.size(); x++){
                    if(playerAction.heading == getHeadingBetween(foodList.get(x))){
                        nabrak = true;
                    }
                }
                // If we can shoot the enemy without colliding with another game object
                if(!this.shootTorpedo && !nabrak){
                    System.out.println("Nembak gan");
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    this.shootTorpedo = true;
                } else {
                    // Chase enemy
                    System.out.println("Kejer musuh gan");
                    this.shootTorpedo = false;
                }
            } else if(foodList.size() > 0){
                // If there is foods nearby
                if(!this.shootTorpedo && musuhList.size() > 0 && getDistanceBetween(musuhList.get(0), this.bot) - musuhList.get(0).size - this.bot.size <= this.bot.size + 150){
                    // If we can shoot enemy nearby
                    playerAction.heading = getHeadingBetween(musuhList.get(0));
                    var nabrak = false;
                    for(int x = 0; x < foodList.size(); x++){
                        if(playerAction.heading == getHeadingBetween(foodList.get(x))){
                            nabrak = true;
                        }
                    }
                    if(!nabrak){
                        // If torpedo will not collide
                        System.out.println("Nembak gan");
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        this.shootTorpedo = true;
                    } else {
                        // Eat food if it collides
                        System.out.println("Kejer makanan gan");
                        playerAction.heading = getHeadingBetween(foodList.get(0));
                        this.shootTorpedo = false;
                    }
                } else {
                    // Eat food
                    System.out.println("Kejer makanan gan");
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    this.shootTorpedo = false;
                }
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
                    if(this.gameState.getWorld().radius - getDistanceBetween(foodList.get(idx), this.gameState.getWorld()) >= sizeUntukEdge(this.gameState.getWorld().radius, this.bot.size)){
                        if(foodList.get(idx).getGameObjectType() == ObjectTypes.FOOD){
                            score += (3.0 / getDistanceBetween(this.bot, foodList.get(idx))) + (3.0 / getDistanceBetween(foodList.get(idx), this.gameState.getWorld()));
                        } else {
                        score += (6.0 / getDistanceBetween(this.bot, foodList.get(idx))) + (6.0 / getDistanceBetween(foodList.get(idx), this.gameState.getWorld()));
                        }
                        tempFoodList.add(foodList.get(idx));
                    }
                }
                idx++;
            }
            // Iterate Player List
            idx = 0;
            var chooseToSkip = false;
            while (idx < playerList.size() && getDistanceBetween(playerList.get(idx), this.bot) - playerList.get(idx).size - this.bot.size <= maxDistance){
                var head = getHeadingBetween(playerList.get(idx));
                double theta = (double) playerList.get(idx).size / getDistanceBetween(playerList.get(idx), this.bot);
                theta = toDegrees(Math.asin(theta));
                var headFirstEnemy = (head - theta) % 360;
                var headLastEnemy = (head + theta) % 360;
                if((headingFirst >= headFirstEnemy && headingFirst <= headLastEnemy) || (headingLast >= headFirstEnemy && headingLast <= headLastEnemy) || (head >= headingFirst && head <= headingLast)){
                    if((double) playerList.get(idx).size * 6.0 / 5.0 <= this.bot.size){
                        score += ((double) playerList.get(idx).size / getDistanceBetween(this.bot, playerList.get(idx)));
                        tempPlayerList.add(playerList.get(idx));
                    } else {
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
        /*System.out.print(this.gameState.getWorld().currentTick);
        System.out.print(" : ");
        System.out.println(maxScore);*/
        return Arrays.asList(fixFoodList, fixPlayerList, fixObstacleList);
    }

    public double sizeUntukEdge (int radius, int size) {
        if (size <= 0.25 * radius) {
            return 1.7 * (double) size;
        }
        else if (size > 0.25 * radius && size <= 0.5 * radius) {
            return 1.4 * (double) size;
        }
        else {
            return 1.2 * (double) size;
        }
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

    private double getDistanceBetween(GameObject object1, World world) {
        var triangleX = Math.abs(object1.getPosition().x - world.getCenterPoint().x);
        var triangleY = Math.abs(object1.getPosition().y - world.getCenterPoint().y);
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
