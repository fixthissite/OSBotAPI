#WebWalker
A to B has never been so simple.

#Features:
- Gets *most* places in the world. (msg me to add more).
- Handles multiple types of objects (msg me to add more).
- Randomized path finding and path interaction

#Debugging
Debug is only printed to the console and if `getWalker().setVerboseLog(true)` was called. This is due to memory leaks assosiated with large amounts of logging to OSBots client.

#The API

The API is accessed in your scripts loop through the `web` variable. As long as you can access this variable, the rest of the web functionality is available.

#Walker (aka `web`)

###Methods:

#####`void getWalker().setVerboseLog(Boolean verbose)`
Determines whether it should spam the console with walking related logs. Use when having issues with walker.

#####`void getWalker().render(Graphics2D g)`
Shows the "Region Loading" rendering, its advised you use this function.
#####`Tile getWalker().tile()`
Returns the players location.

#####`Tile getWalker().tile(Entity entity)`
#####`Tile getWalker().tile(Position position)`
#####`Tile getWalker().tile(int x, int y, int z)`
Returns a `Tile`for the given Entity/Position/Coords.

#####`Tile getWalker().tile(String s)`
Converts a string formatted like `XXXX,YYYY,Z` to a `Tile`

#####`GlobalPath getWalker().findPath(Entity entity)`
#####`GlobalPath getWalker().findPath(Position position)`
#####`GlobalPath getWalker().findPath(Tile tile)`
Finds a `GlobalPath` to the given location.


#####`LocalPath getWalker().findLocalPath(Entity entity)`
#####`LocalPath getWalker().findLocalPath(Position position)`
#####`LocalPath getWalker().findLocalPath(Tile tile)`
Finds a `LocalPath` to the given location.

#####`void getWalker().regionContains(Entity entity)`
#####`void getWalker().regionContains(Position position)`
#####`void getWalker().regionContains(Tile tile)`
Checks if the region contains the tiles. If a this returns `true` it means you can `LocalPath` to it.

#####`boolean getWalker().canReach(Entity entity)`
#####`boolean getWalker().canReach(Position position)`
#####`boolean getWalker().canReach(Tile tile)`
Determines if the tile can be reached by the player without interacting with objects.

#####`void getWalker().wait(Entity entity)`
#####`void getWalker().wait(Position position)`
#####`void getWalker().wait(Tile tile)`
Waits until the player has moved near the given location/tile.

#####`void getWalker().wait(Entity entity, WalkListener<Boolean, Tile>)`
#####`void getWalker().wait(Position position, WalkListener<Boolean, Tile>)`
#####`void getWalker().wait(Tile tile, WalkListener<Boolean, Tile>)`
Similar to `getWalker().wait()`, but also allows a WaitListener to be called which can end the
wait early.

#####`Tile getWalker().findEntityEnd(Entity entity)`
Finds the closest open tile for an entity of any given size.

#####`Tile getWalker().getFlag()`
Returns the `Tile` of the minimap flag only if valid, else it returns `null`.

#####`Tile getWalker().setHandleRun(boolean handleRun)`
Defaults to `true`, will determine if the walker automatically manages running levels or not.

#GlobalPath
###Methods

#####`boolean GlobalPath.walk()`
#####`boolean GlobalPath.walk(WalkListener<Boolean, Tile> callable)`
Walks a step of the path. Returns `true` if the path is finished. For `WalkListener` usage please see the section below.

A step is defined in `LocalPath.walk()`.

#####`boolean GlobalPath.loop()`
#####`boolean GlobalPath.loop(int times)`
#####`boolean GlobalPath.loop(WalkListener<Boolean, Tile> callable)`
#####`boolean GlobalPath.loop(WalkListener<Boolean, Tile> callable, int times)`
Runs `GlobalPath.walk` in a loop `times` (default `20`) times. For `WalkListener` usage please see the section below.

#####`boolean GlobalPath.valid()`
Returns `true` if the path is valid and walkable.

#####`int GlobalPath.length()`
Returns the length of the tiles in the path. Note for distance measures `Tile.actualDistanceTo()` should be used.

#LocalPath
###Example:
###Methods

#####`boolean LocalPath.walk()`
#####`boolean LocalPath.walk(WalkListener<Boolean, Tile> callable)`
Walks a step of the path. Returns `true` if the path is finished. For `WalkListener` usage please see the section below.

A step is 1 mini map interaction, then a wait until it is near the path end.

#####`boolean LocalPath.valid()`
Returns `true` if the path is valid and walkable.


#####`void LocalPath.loop()`
#####`void LocalPath.loop(int loops)`
#####`void LocalPath.loop(WalkListener<Boolean, Tile> callable)`
#####`void LocalPath.loop(WalkListener<Boolean, Tile> callable, int times)`
Runs `LocalPath.walk` in a loop `times` (or `20`) times. For `WalkListener` usage please see the section below.

#Tile
#####`int Tile.actualDistanceTo()`
#####`int Tile.actualDistanceTo(Entity e)`
#####`int Tile.actualDistanceTo(Position p)`
#####`int Tile.actualDistanceTo(Tile t)`
Finds a `LocalPath` from this tile to either the given object, or to the players current position if no arguments are passed.

#####`boolean Tile.canReach()`
#####`boolean Tile.canReach(boolean failOnObstacles)`
Will test if this tile can be reached, or rather if a `LocalPath` could be found to it. If `failOnObstacles` is `true` it will return `false` when an obstacles needs to be solved is blocking a path.

#####`boolean Tile.clickOnMap()`
Will click on a random tile near this position, accounting for walls and objects in the way (won't click inside/outside a house if the `Tile` is outside/inside the house).

#####`boolean Tile.compare(Entity e)`
#####`boolean Tile.compare(Position p)`
#####`boolean Tile.compare(Tile t)`
#####`boolean Tile.compare(int x, int y, int z)`
Compares a `Tile` to this `Tile`, returns `true` if they match. Note that tiles should be comparable with `Tile == Tile`, but to be safe theres a method for it.

#####`boolean Tile.distanceTo()`
#####`boolean Tile.distanceTo(Entity e)`
#####`boolean Tile.distanceTo(Position p)`
#####`boolean Tile.distanceTo(Tile t)`
Calculates the distance to the given object, or to the players current position if no arguments are passed.

#####`boolean Tile.distanceTo()`
#####`boolean Tile.distanceTo(Entity e)`
#####`boolean Tile.distanceTo(Position p)`
#####`boolean Tile.distanceTo(Tile t)`
Calculates the distance to the given object, or to the players current position if no arguments are passed.

#####`boolean Tile.hover()`
Hovers the tile.

#####`boolean Tile.isVisible()`
Returns if this `Tile` is visible.

#####`Position Tile.pos()`
Returns the old `Position` you all know and love.

#####`Tile Tile.translate(int x, int y)`
#####`Tile Tile.translate(int x, int y, int z)`
Returns a new `Tile` that is offset by the given amounts. Note that given amounts can be negative or positive.

#####`boolean Tile.within(int x1, int y1, int x2, int y2)`
#####`boolean Tile.within(Tile t1, Tile t2)`
Checks if the `x` and `y` values are within the given range.

#Example Code

###For simple loop codes
For any code that just uses 1 large block of code, all you need to do is create a new field, say myPath:

```java
private GlobalPath myPath;
```
then at the top of our `loop()`, we put:
```java
if (myPath != null && myPath.valid()) { // check the paths validity
    if (myPath.walk())
        myPath = null; // Since walk returns true when done, we null the path for GC.
}
```
This chunk of code will always run when a path is given to `myPath`, so anywhere in our code we need to walk we just do:
```java
myPath = getWalker().findPath(position);
return 0; // Return and start walking immediately
```
Then you should be walking!
###For getState()
For a simple `getState()` setup, we do this:
```java
private GlobalPath myPath;

public int getState() {
    if (myPath != null && myPath.valid())
        return WALKING;
    if (someOtherCondtion)
        return SOME_STATE;
}

// This function is called when the state is WALKING
public void onWalking() {
    if (myPath.walk())
        myPath = null;
}

// This function is called when state is SOME_STATE
public void onSomeState() {
    // We need to walk to someplace
    myPath = getWalker().findPath(position);
}
```