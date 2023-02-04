package Models;

public class World {

  public Position centerPoint;
  public Integer radius;
  public Integer currentTick;

  public Position getCenterPoint() {
    return this.centerPoint;
  }

  public void setCenterPoint(Position centerPoint) {
    this.centerPoint = centerPoint;
  }

  public Integer getRadius() {
    return this.radius;
  }

  public void setRadius(Integer radius) {
    this.radius = radius;
  }

  public Integer getCurrentTick() {
    return this.currentTick;
  }

  public void setCurrentTick(Integer currentTick) {
    this.currentTick = currentTick;
  }
}
