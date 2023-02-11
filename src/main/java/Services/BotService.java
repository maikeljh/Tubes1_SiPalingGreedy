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
    private int headingTeleporter;
    private int shootTeleporter;
    private boolean shieldActive;
    private int tickShield;
    private boolean afterBurnerActive;
    private boolean shootSupernova;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        this.shootTorpedo = false;
        this.headingTeleporter = -999;
        this.shootTeleporter = 0;
        this.shieldActive = false;
        this.tickShield = -999;
        this.afterBurnerActive = false;
        this.shootSupernova = false;
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
        // Output Current Tick
        if(this.gameState.getWorld().getCurrentTick() != null){
            System.out.print(this.gameState.getWorld().getCurrentTick());
            System.out.print(": ");
        }
        // Initialize action and heading
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);
        // While game objects not empty
        if (!this.gameState.getGameObjects().isEmpty()) {
            // Getting object list
            var objectList = this.gameState.getGameObjects();
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
            // Getting teleport list to avoid
            var teleportList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                    .collect(Collectors.toList());
            // Getting obstacle list to avoid
            var obstacleList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                    .sorted(Comparator
                        .comparing(obstacle -> getDistanceBetween(this.bot, obstacle)))
                    .collect(Collectors.toList());
            // Getting torpedo list to activate shield
            var torpedoList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                    .sorted(Comparator
                        .comparing(torpedo -> getDistanceBetween(this.bot, torpedo)))
                    .collect(Collectors.toList());
            // Getting supernova list to get and shoot supernova
            var supernovaList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP)
                    .collect(Collectors.toList());
            var supernovaBombList = this.gameState.getGameObjects()
                    .stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB)
                    .collect(Collectors.toList());

            // Choosing best area
            var chosenArea = scanArea(foodList, playerList, obstacleList, supernovaList);
            foodList = chosenArea.get(0);
            playerList = chosenArea.get(1);
            obstacleList = chosenArea.get(2);
            supernovaList = chosenArea.get(3);

            // Checking available teleport
            GameObject myTeleporter = null;
            if(checkOwnerTeleporter(teleportList)){
                myTeleporter = getTeleporter(teleportList);
            }
            
            // Checking shield active or not
            if(this.tickShield != -999 && this.gameState.getWorld().getCurrentTick() - this.tickShield >= 20){
                this.tickShield = -999;
                this.shieldActive = false;
            }

            // If we can teleport now
            if(this.shootSupernova && checkDetonateSupernova(supernovaBombList, musuhList)){
                playerAction.action = PlayerActions.DETONATESUPERNOVA;
                System.out.println("LEDAKIN GAN");
                this.shootSupernova = false;
            } else if(this.bot.supernovaAvailable > 0 && checkFireSupernova(musuhList)){
                playerAction.action = PlayerActions.FIRESUPERNOVA;
                var musuhGedeList = this.gameState.getPlayerGameObjects()
                    .stream()
                    .filter(enemy -> enemy.id != this.bot.id)
                    .sorted(Comparator
                        .comparing(enemy -> enemy.size))
                    .collect(Collectors.toList());
                for(int i = musuhGedeList.size() - 1; i >= 0; i--){
                    if(getDistanceBetween(this.bot, musuhGedeList.get(i)) >= 300){
                        playerAction.heading = getHeadingBetween(musuhGedeList.get(i));
                        break;
                    }
                }
                System.out.println("TEMBAKKK");
                this.shootSupernova = true;
            } else if(supernovaList.size() > 0){
                playerAction.heading = getHeadingBetween(supernovaList.get(0));
                System.out.println("Kejer Supernova");
                if(getDistanceBetween(this.bot, supernovaList.get(0)) < 30){
                    System.out.println("Deket Supernova Bang");
                }
            } else if(myTeleporter != null && musuhList.size() > 0 && doTeleport(myTeleporter, musuhList)){
                playerAction.action = PlayerActions.TELEPORT;
                this.headingTeleporter = -999;
                this.shootTeleporter = 0;
                System.out.println("Nguing");
            } else if(torpedoList.size() > 0 && checkShieldCondition(torpedoList) && !this.shieldActive){
                playerAction.action = PlayerActions.ACTIVATESHIELD;
                this.shieldActive = true;
                this.tickShield = this.gameState.getWorld().getCurrentTick();
                System.out.println("Aktifin Shield Gan");
            }
            else if (musuhList.size() > 0 && !checkEnemyChase(musuhList) && this.afterBurnerActive) {
                playerAction.action = PlayerActions.STOPAFTERBURNER;
                this.afterBurnerActive = false;
                System.out.println("Stop afterburner gan");
            }
            else if (musuhList.size() > 0 && checkEnemyChase(musuhList) && !this.afterBurnerActive) {
                playerAction.action = PlayerActions.STARTAFTERBURNER;
                this.afterBurnerActive = true;
                System.out.println("Aktifin afterburner gan");
            }
            else if(this.shootTeleporter == 0 && musuhList.size() > 0 && getDistanceBetween(this.bot, musuhList.get(0)) >= 100 && getDistanceBetween(this.bot, musuhList.get(0)) < 400 && this.bot.teleporterCount > 0 && this.headingTeleporter == -999 && this.bot.size > 60 && this.bot.size - 40 >= musuhList.get(0).size && musuhList.get(0).size >= 30){
                var head = getHeadingBetween(musuhList.get(0));
                playerAction.heading = head;
                this.headingTeleporter = head;
                playerAction.action = PlayerActions.FIRETELEPORT;
                this.shootTeleporter = 2;
                System.out.println("Tembak Teleport Gan");
            } else if(foodList.size() == 0 && playerList.size() == 0){
                // If there is no food and enemy nearby
                if(!this.shootTorpedo && musuhList.size() > 0 && getDistanceBetween(musuhList.get(0), this.bot) - musuhList.get(0).size - this.bot.size <= this.bot.size + 150){
                    playerAction.heading = getHeadingBetween(musuhList.get(0));
                    var nabrak = false;
                    for(int x = 0; x < objectList.size(); x++){
                        if(Math.abs(playerAction.heading - getHeadingBetween(objectList.get(x))) <= 10){
                            nabrak = true;
                        }
                    }
                    if(!nabrak && musuhList.get(0).size >= 10){
                        System.out.println("Nembak gan");
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        this.shootTorpedo = true;
                    } else if(foodNotNearEdgeList.size() > 0){
                        playerAction.heading = getHeadingBetween(foodNotNearEdgeList.get(0));
                        System.out.println("Cari makan yang gak di ujung");
                        this.shootTorpedo = false;
                    } else {
                        System.out.println("Ke tengah bang");
                        playerAction.heading = getHeadingBetween(this.gameState.getWorld());
                        this.shootTorpedo = false;
                    }
                }
            } else if(playerList.size() > 0 && playerList.get(0).size < this.bot.size){
                // If there is a smaller enemy nearby
                playerAction.heading = getHeadingBetween(playerList.get(0));
                var nabrak = false;
                for(int x = 0; x < objectList.size(); x++){
                    if(Math.abs(playerAction.heading - getHeadingBetween(objectList.get(x))) <= 10){
                        nabrak = true;
                    }
                }
                // If we can shoot the enemy without colliding with another game object and enemy size >= 15
                if(!this.shootTorpedo && !nabrak && playerList.get(0).size >= 15){
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
                            break;
                        }
                    }
                    if(!nabrak && (musuhList.get(0).size >= 20 || (musuhList.get(0).size >= 10 && getDistanceBetween(this.bot, musuhList.get(0)) < 100))){
                        // If torpedo will not collide and enemy size >= 20
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

            if(this.shootTeleporter > 0){
                this.shootTeleporter--;
            }
        }

        this.playerAction = playerAction;
    }

    public List<List<GameObject>> scanArea(List<GameObject> foodList, List<GameObject> playerList, List<GameObject> obstacleList, List<GameObject> supernovaList){
        // Initialize area
        var area = 0;
        double maxScore = 0;
        List<GameObject> fixFoodList = new ArrayList<GameObject>();
        List<GameObject> fixPlayerList = new ArrayList<GameObject>();
        List<GameObject> fixObstacleList = new ArrayList<GameObject>();
        List<GameObject> fixSupernovaList = new ArrayList<GameObject>();

        for(int i = 0; i < 12; i++){
            var headingFirst = i * 30;
            var headingLast = headingFirst + 30;
            var maxDistance = 250;
            double score = 0;
            List<GameObject> tempFoodList = new ArrayList<GameObject>();
            List<GameObject> tempPlayerList = new ArrayList<GameObject>();
            List<GameObject> tempObstacleList = new ArrayList<GameObject>();
            List<GameObject> tempSupernovaList = new ArrayList<GameObject>();

            // Iterate Food List
            var idx = 0;
            while (idx < foodList.size() && getDistanceBetween(foodList.get(idx), this.bot) <= maxDistance){
                var head = getHeadingBetween(foodList.get(idx));
                if(head >= headingFirst && head <= headingLast){
                    if(this.gameState.getWorld().radius - getDistanceBetween(foodList.get(idx), this.gameState.getWorld()) >= sizeUntukEdge(this.gameState.getWorld().radius, this.bot.size)){
                        if(foodList.get(idx).getGameObjectType() == ObjectTypes.FOOD){
                            score += (3.0 / (getDistanceBetween(this.bot, foodList.get(idx)) - this.bot.size) + (3.0 / getDistanceBetween(foodList.get(idx), this.gameState.getWorld())));
                            for (int j = 0; j < playerList.size() ; j++) {
                                if (playerList.get(j).size > this.bot.size && getDistanceBetween(this.bot, playerList.get(j)) - this.bot.size - playerList.get(j).size <= 50 && getDistanceBetween(foodList.get(idx), playerList.get(j)) - playerList.get(j).size > getDistanceBetween(this.bot, foodList.get(idx)) - this.bot.size) {
                                    score -= (3.0 / (getDistanceBetween(foodList.get(idx), playerList.get(j)) - playerList.get(j).size));
                                }
                            }
                        } else {
                            score += (6.0 / (getDistanceBetween(this.bot, foodList.get(idx)) - this.bot.size) + (6.0 / getDistanceBetween(foodList.get(idx), this.gameState.getWorld())));
                            for (int j = 0; j < playerList.size() ; j++) {
                                if (playerList.get(j).size > this.bot.size && getDistanceBetween(this.bot, playerList.get(j)) - this.bot.size - playerList.get(j).size <= 50 && getDistanceBetween(foodList.get(idx), playerList.get(j)) - playerList.get(j).size > getDistanceBetween(this.bot, foodList.get(idx)) - this.bot.size) {
                                    score -= (6.0 / (getDistanceBetween(foodList.get(idx), playerList.get(j)) - playerList.get(j).size));
                                }
                            }
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
                    if(obstacleList.get(idx).getGameObjectType() == ObjectTypes.GAS_CLOUD){
                        if(playerList.size() > 0 && getDistanceBetween(playerList.get(0), this.bot) > maxDistance && getDistanceBetween(obstacleList.get(idx), this.bot) - this.bot.size <= 75){
                            chooseToSkip = true;
                            break;
                        } else {
                            score -= (obstacleList.get(idx).size / getDistanceBetween(this.bot, obstacleList.get(idx)));
                            tempObstacleList.add(obstacleList.get(idx));
                        }
                    } else {
                        score -= (obstacleList.get(idx).size / getDistanceBetween(this.bot, obstacleList.get(idx)));
                        tempObstacleList.add(obstacleList.get(idx));
                    }
                }
                idx++;
            }
            if(chooseToSkip){
                break;
            }

            // Iterate Supernova List
            idx = 0;
            while (idx < supernovaList.size() && getDistanceBetween(supernovaList.get(idx), this.bot) <= maxDistance){
                var head = getHeadingBetween(supernovaList.get(idx));
                if(head >= headingFirst && head <= headingLast){
                    score += 10.0 / getDistanceBetween(this.bot, supernovaList.get(idx));
                    var minDistanceSupernova = getDistanceBetween(supernovaList.get(idx), playerList.get(0));
                    var idxSupernova = 0;
                    for(int k = 1; k < playerList.size(); k++){
                        var check = getDistanceBetween(supernovaList.get(idx), playerList.get(k));
                        if(check < minDistanceSupernova){
                            check = minDistanceSupernova;
                            idxSupernova = k;
                        }
                    }
                    score -= 10.0 / getDistanceBetween(playerList.get(idxSupernova), supernovaList.get(idx));
                    tempSupernovaList.add(supernovaList.get(idx));
                }
                idx++;
            }

            // Choose area
            if(score > maxScore){
                maxScore = score;
                area = i;
                fixFoodList = tempFoodList;
                fixPlayerList = tempPlayerList;
                fixObstacleList = tempObstacleList;
                fixSupernovaList = tempSupernovaList;
            }
        }
        /*System.out.print(this.gameState.getWorld().currentTick);
        System.out.print(" : ");
        System.out.println(maxScore);*/
        return Arrays.asList(fixFoodList, fixPlayerList, fixObstacleList, fixSupernovaList);
    }

    public boolean checkFireSupernova(List<GameObject> musuhList){
        var check = false;     
        for(int i = 0; i < musuhList.size(); i++){
            if(getDistanceBetween(musuhList.get(i), this.bot) >= 300){
                check = true;
                break;
            }
        }
        return check;
    }

    public boolean checkDetonateSupernova(List<GameObject> supernovaList, List<GameObject> musuhList){
        if(supernovaList.size() == 0){
            return false;
        } else {
            var supernova = supernovaList.get(0);
            if(getDistanceBetween(supernova, this.bot) - this.bot.size <= 75){
                return false;
            } else {
                var check = false;
                for(int i = 0; i < musuhList.size(); i++){
                    if(getDistanceBetween(musuhList.get(i), supernova) <= 100){
                        check = true;
                        break;
                    }
                }
                return check;
            }
        }
    }

    public boolean checkShieldCondition(List<GameObject> torpedoList){
        var count = 0;
        for(int i = 0; i < torpedoList.size(); i++){
            if(Math.abs(getHeadingObject(torpedoList.get(i)) - torpedoList.get(i).currentHeading) <= 10 && getDistanceBetween(torpedoList.get(i), this.bot) < 200){
                count++;
                if(i + 1 < torpedoList.size()){
                    for(int j = i + 1; j < torpedoList.size(); j++){
                        if(Math.abs(getHeadingObject(torpedoList.get(j)) - torpedoList.get(j).currentHeading) <= 10 && getDistanceBetween(torpedoList.get(j), torpedoList.get(i)) < 200){
                            count++;
                        }
                    }
                }
                break;
            }
        }
        if(count > 1 && this.bot.size > 40){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkEnemyChase(List <GameObject> musuhList) {
        var count = 0;
        for (int i = 0; i < musuhList.size(); i++) {
            if(Math.abs(getHeadingObject(musuhList.get(i)) - musuhList.get(i).currentHeading) <= 10 && getDistanceBetween(musuhList.get(i), this.bot) < 200 && musuhList.get(i).size > this.bot.size) {
                count++;
            }
        }
        if (count >= 1 && this.bot.size >= 20) {
            System.out.println("True min");
            return true;
        }
        else {
            return false;
        }
    }

    public boolean checkOwnerTeleporter(List<GameObject> teleportList){
        for(int i = 0; i < teleportList.size(); i++){
            if(Math.abs(teleportList.get(i).currentHeading - this.headingTeleporter) <= 3){
                return true;
            }
        }
        return false;
    }

    public GameObject getTeleporter(List<GameObject> teleportList){
        for(int i = 0; i < teleportList.size(); i++){
            if(Math.abs(teleportList.get(i).currentHeading - this.headingTeleporter) <= 3){
                return teleportList.get(i);
            }
        }
        return null;
    }

    public boolean doTeleport(GameObject myTeleport, List<GameObject> musuhList){
        var minDistance = getDistanceBetween(myTeleport, musuhList.get(0));
        GameObject musuhIncar = musuhList.get(0);
        for(int i = 1; i < musuhList.size(); i++){
            var jarak = getDistanceBetween(myTeleport, musuhList.get(i));
            if(jarak < minDistance){
                minDistance = jarak;
                musuhIncar = musuhList.get(i);
            }
        }
        if(minDistance - this.bot.size - musuhIncar.size < 50 && musuhIncar.size < this.bot.size){
            return true;
        } else {
            return false;
        }
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

    private int getHeadingObject(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(bot.getPosition().y - otherObject.getPosition().y,
                bot.getPosition().x - otherObject.getPosition().x));
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
