import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

@ScriptManifest(name = "Scrub's Draynor Willow Killer", description = "My first woodcutting bot", author = "Scrubthebot",
        version = 1.0, category = Category.WOODCUTTING, image = "")
public class Woodcutter extends AbstractScript {

    Area bankArea = new Area(3092, 3246, 3095, 3240);
    Area willowTreeArea = new Area(3082, 3238, 3090, 3225);
    State state;

    @Override
    public int onLoop() {
        switch(getState()) {
            case WALKING_TO_BANK:
                if (!Players.getLocal().isMoving()) {
                    Walking.walk(bankArea.getRandomTile());
                }
                break;
            case USEBANK:
                if (!Bank.isOpen() && bankArea.contains(Players.getLocal())) {
                    GameObject bankBooth = GameObjects.closest(b -> "Bank booth".equalsIgnoreCase(b.getName()));
                    bankBooth.interactForceLeft("Bank");
                    Logger.log(bankBooth.getObjectTiles());
                    Sleep.sleepUntil(() -> Bank.isOpen(), 4000);
                }
                break;
            case BANKING:
                Sleep.sleep(500, 2000);
                Bank.depositAllItems();
                Sleep.sleep(300, 1200);
                Sleep.sleepUntil(() -> !Inventory.contains("Willow logs"), 2000);
                if (!Inventory.contains("Willow logs")) {
                    Walking.walk(willowTreeArea.getRandomTile());
                    Sleep.sleep(1000, 10000);
                }
                break;
            case WALKING_TO_TREES:
                if (!Players.getLocal().isMoving()) {
                    Walking.walk(willowTreeArea.getRandomTile());
                }
                break;
            case FINDING_TREE:
                if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                    Sleep.sleep(1000, 10000);
                    GameObject willowTree = GameObjects.closest(t -> t.getName().equalsIgnoreCase("Willow tree") && willowTreeArea.contains(t.getTile()));
                    if (willowTree != null && willowTree.interact("Chop down")) {
                        Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 6000);
                        Mouse.moveOutsideScreen(true);
                    }
                }
                break;
        }
        return 1;
    }

    private State getState() {
        if (Inventory.isFull() && !bankArea.contains(Players.getLocal().getTile())) {
            return State.WALKING_TO_BANK;
        }
        else if (Inventory.isFull() && bankArea.contains(Players.getLocal().getTile()) && !Bank.isOpen()) {
            return State.USEBANK;
        }
        else if (Bank.isOpen() && Inventory.isFull()) {
            return State.BANKING;
        }
        else if (!Inventory.isFull() && !willowTreeArea.contains(Players.getLocal().getTile())) {
            return State.WALKING_TO_TREES;
        }
        else if (!Inventory.isFull() && !Players.getLocal().isAnimating() && willowTreeArea.contains(Players.getLocal().getTile())) {
            return State.FINDING_TREE;
        }
        else if (!Inventory.isFull() && Players.getLocal().isAnimating() && willowTreeArea.contains(Players.getLocal().getTile())) {
            return State.CHOPPING_TREE;
        }
        return state;
    }
}