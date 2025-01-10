package pebbleprojects.strongMCPvP.functions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GUIClick {

    private final GUI gui;
    private final int slot;
    private final Player player;
    private final ItemStack clickedItem;
    private final boolean leftClick, rightClick, shiftClick, middleClick;

    public GUIClick(final Player player, final int slot, final boolean leftClick, final boolean rightClick, final boolean shiftClick, final boolean middleClick, final ItemStack clickedItem, final GUI gui) {
        this.gui = gui;
        this.slot = slot;
        this.player = player;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftClick = shiftClick;
        this.middleClick = middleClick;
        this.clickedItem = clickedItem;
    }

    public GUI getGUI() {
        return gui;
    }

    public int getSlot() {
        return slot;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isLeftClick() {
        return leftClick;
    }

    public boolean isRightClick() {
        return rightClick;
    }

    public boolean isShiftClick() {
        return shiftClick;
    }

    public boolean isMiddleClick() {
        return middleClick;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }
}
