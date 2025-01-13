package pebbleprojects.strongMCPvP.functions.perks;

public final class PerkSlot {

    private int perkId;
    private final int perkSlot;

    public PerkSlot(final String data) {
        this(Integer.parseInt(data.split(",")[0]), Integer.parseInt(data.split(",")[1]));
    }

    public PerkSlot(final int perkId, final int perkSlot) {
        this.perkId = perkId;
        this.perkSlot = perkSlot;
    }

    public void setPerkId(final int perkId) {
        this.perkId = perkId;
    }

    public int getPerkId() {
        return perkId;
    }

    public int getPerkSlot() {
        return perkSlot;
    }

    @Override
    public String toString() {
        return perkId + "," + perkSlot;
    }
}
