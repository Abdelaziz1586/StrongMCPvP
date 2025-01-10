package pebbleprojects.strongMCPvP.functions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Perk {

    private final int price;
    private final ItemStack guiItem;
    private final Configuration config;
    private final Map<String, Object> staticVariables;
    private final Map<Player, Map<String, Object>> uniqueVariables;

    public Perk(final int price, final ItemStack guiItem, final Configuration perkSection) {
        this.price = price;
        this.guiItem = guiItem;
        this.config = perkSection;

        staticVariables = new ConcurrentHashMap<>();
        uniqueVariables = new ConcurrentHashMap<>();

        final Configuration variablesSection = config.getSection("variables");
        if (variablesSection != null) {
            for (final String key : variablesSection.getKeys()) {
                staticVariables.put(key, variablesSection.get(key));
            }
        }
    }

    public int getPrice() {
        return price;
    }

    public ItemStack getGuiItem() {
        return guiItem;
    }

    public void onEntityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = null;
        final Player victim = (Player) event.getEntity();
        if (event instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;

            if (entityDamageByEntityEvent.getDamager() instanceof Player) {
                attacker = (Player) entityDamageByEntityEvent.getDamager();
            } else if (entityDamageByEntityEvent.getDamager() instanceof Projectile) {
                final ProjectileSource source = ((Projectile) entityDamageByEntityEvent.getDamager()).getShooter();

                if (source instanceof Player) {
                    attacker = (Player) source;
                }
            }
        }

        if (event.getFinalDamage() >= victim.getHealth()) {
            executeSection("kill", attacker, victim, event);
            return;
        }

        executeSection("damage", attacker, victim, event);
    }

    public void onPlayerSpawn(final Player player) {
        executeSection("spawn", player, null, player);
    }

    public void onPlayerDeath(final Player victim, final Player attacker) {
        executeSection("death", victim, attacker, victim);
    }

    public void onPlayerClick(final PlayerInteractEvent event) {
        executeSection("click", event.getPlayer(), null, event);
    }

    private void executeSection(final String sectionName, final Player player1, final Player player2, final Object event) {
        final Configuration section = config.getSection(sectionName);
        if (section != null) {
            executeActionsWithConditions(section, player1, player2, event, true);
        }
    }

    private void executeActionsWithConditions(final Configuration section, final Player player1, final Player player2, final Object event, final boolean handleWait) {
        for (final String key : section.getKeys()) {
            if (key.toLowerCase().startsWith("if ")) {
                handleConditionalActions(section, key, player1, player2, event);
                continue;
            }

            if (key.toLowerCase().startsWith("wait ")) {
                if (handleWait) handleWaitActions(section, key, player1, player2, event);
                continue;
            }

            if (key.equalsIgnoreCase("execute")) {
                handleExecuteActions(section.getStringList(key), player1, player2, event);
                continue;
            }

            executeAction(key, section.get(key).toString(), player1, player2, event);
        }
    }

    private void handleConditionalActions(final Configuration section, final String conditionKey, final Player player1, final Player player2, final Object event) {
        final String condition = conditionKey.substring(3);
        final boolean conditionMet = evaluateCondition(condition, event);

        if (conditionMet) {
            executeActionsWithConditions(section.getSection(conditionKey), player1, player2, event, true);
            return;
        }

        final Configuration elseSection = section.getSection("else");
        if (elseSection != null) {
            executeActionsWithConditions(elseSection, player1, player2, event, true);
        }
    }

    private void handleExecuteActions(final List<String> actions, final Player player1, final Player player2, final Object event) {
        for (final String action : actions) {
            final String[] split = action.split(": ", 2);
            if (split.length != 2) continue;

            executeAction(split[0], split[1], player1, player2, event);
        }
    }

    private void handleWaitActions(final Configuration section, final String waitKey, final Player player1, final Player player2, final Object event) {
        final String wait = waitKey.substring(5);

        final String[] parts = wait.split(" ", 2);
        if (parts.length != 2) return;

        final long multiply;
        switch (parts[1].toLowerCase()) {
            case "second":
            case "seconds":
                multiply = 20L;
                break;
            case "minute":
            case "minutes":
                multiply = 1200L;
                break;
            case "hour":
            case "hours":
                multiply = 72000L;
                break;
            default:
                multiply = 1L;
                break;
        }

        int delay;
        try {
            delay = Integer.parseInt(parts[0]);
        } catch (final NumberFormatException ignored) {
            delay = 0;
        }

        if (delay < 0) delay = 0;

        TaskHandler.INSTANCE.runLaterAsync(() -> executeActionsWithConditions(section.getSection(waitKey), player1, player2, event, false), delay * multiply);
    }

    private boolean evaluateCondition(final String conditionString, final Object event) {
        final String[] orConditions = conditionString.contains(" OR ") ? conditionString.split(" OR ") : new String[]{conditionString};

        for (final String orCondition : orConditions) {
            final String[] andConditions = orCondition.split(" AND ");
            boolean andResult = true;
            for (final String condition : andConditions) {
                final String[] parts = condition.trim().split("=");
                if (parts.length != 2) {
                    andResult = false;
                    break;
                }

                final String key = resolveVariable(parts[0].trim(), getPlayerFromEvent(event)),
                        value = resolveVariable(parts[1].trim(), getPlayerFromEvent(event));

                final boolean singleConditionMet = evaluateSingleCondition(key, value, event);
                if (!singleConditionMet) {
                    andResult = false;
                    break;
                }
            }
            if (andResult) return true;
        }
        return false;
    }

    private boolean evaluateSingleCondition(final String key, final String value, final Object event) {
        switch (key) {
            case "cause":
                return event instanceof EntityDamageEvent &&
                        ((EntityDamageEvent) event).getCause().name().equals(value);
            case "item":
                return event instanceof PlayerInteractEvent &&
                        ((PlayerInteractEvent) event).getItem() != null &&
                        ((PlayerInteractEvent) event).getItem().getType().name().equals(value);
            case "click":
                return event instanceof PlayerInteractEvent &&
                        ((PlayerInteractEvent) event).getAction().name().contains(value);
            case "type":
                return event instanceof EntityDamageByEntityEvent &&
                        ((EntityDamageByEntityEvent) event).getDamager().getType().name().equals(value);
            case "selfHit":
                return event instanceof EntityDamageByEntityEvent &&
                        ((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile &&
                        (Boolean.parseBoolean(value) == ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter().equals(((EntityDamageByEntityEvent) event).getEntity()));
            default:
                return key.equals(value);
        }
    }

    private void executeAction(final String actionType, final String actionValue, final Player player1, final Player player2, final Object event) {
        switch (actionType) {
            case "give":
                handleGiveAction(actionValue, player1, player2);
                break;
            case "message":
                handleMessageAction(actionValue, player1, player2);
                break;
            case "removeItem":
                handleRemoveItemAction(actionValue, player1, player2);
                break;
            case "addEffect":
                handleAddEffect(actionValue, player1, player2);
                break;
            case "removeEffect":
                handlerRemoveEffect(actionValue, player1, player2);
                break;
            case "playSound":
                handlePlaySound(actionValue, player1, player2);
                break;
            case "playSoundGlobally":
                handlePlaySoundGlobally(actionValue, player1, player2);
                break;
            case "setVariable":
                handleSetVariableAction(actionValue, player1, player2);
                break;
            case "cancel-event":
                if (Boolean.parseBoolean(actionValue) && event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                }
                break;
        }
    }

    private void handleGiveAction(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",");
        if (parts.length < 2) return;

        final Player target = getPlayerFromString(parts[0], player1, player2);
        if (target == null) return;

        final String textures;
        final Material material;
        if (parts[1].startsWith("[") && parts[1].endsWith("]")) {
            parts[1] = parts[1].split("\\[")[1].split("]")[0];
            final String[] materialParts = parts[1].split(";", 2);
            if (materialParts.length != 2 || !materialParts[0].equalsIgnoreCase("SKULL_ITEM")) return;

            textures = materialParts[1];
            material = Material.SKULL_ITEM;
        } else {
            textures = null;
            material = Material.getMaterial(parts[1].toUpperCase());
        }

        if (material == null) return;

        final ItemStack item = new ItemStack(material, parts.length > 2 ? parseInt(parts[2]) : 1);
        if (parts.length > 3) {
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(translateColorCodes(parts[3]));

            if (parts.length > 4) {
                final List<String> lore = new ArrayList<>(parts.length - 4);
                for (int i = 4; i < parts.length; i++) {
                    lore.add(translateColorCodes(parts[i]));
                }
                meta.setLore(lore);
            }

            if (textures != null) {
                item.setDurability((short) 3);

                final SkullMeta skull = (SkullMeta) meta;

                skull.setOwner("Notch");
                final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", textures));

                try {
                    final Field profileField = skull.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skull, profile);
                } catch (final NoSuchFieldException | IllegalAccessException e) {
                    throw new Error(e);
                }

                item.setItemMeta(skull);
            } else {
                item.setItemMeta(meta);
            }
        }

        target.getInventory().addItem(item);
    }

    private void handleMessageAction(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",", 2);
        if (parts.length != 2) return;

        final Player target = getPlayerFromString(parts[0], player1, player2);
        if (target != null) {
            target.sendMessage(translateColorCodes(resolveVariable(parts[1], target)));
        }
    }

    private void handleRemoveItemAction(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",", 4);
        if (parts.length == 0) return;

        final Player target = getPlayerFromString(parts[0], player1, player2);

        if (target == null) return;

        switch (parts.length) {
            case 1:
                removeItem(target, 1, target.getItemInHand().getType(), null);
                break;
            case 2:
                removeItem(target, 1, Material.getMaterial(parts[1]), null);
                break;
            case 3:
                removeItem(target, 1, Material.getMaterial(parts[1]), translateColorCodes(parts[2]));
                break;
            case 4:
                if (parts[2].equalsIgnoreCase("all")) {
                    target.getInventory().remove(target.getItemInHand());
                    return;
                }

                try {
                    removeItem(target, Integer.parseInt(parts[3]), Material.getMaterial(parts[1]), translateColorCodes(parts[2]));
                } catch (final NumberFormatException ignored) {}
                break;
        }
    }

    private void handleAddEffect(final String actionValue, final Player player1, final Player player2) {
        final List<String> effects = actionValue.startsWith("[") && actionValue.endsWith("]") ? Arrays.asList(actionValue.substring(1, actionValue.length() - 1).split(", ")) : Collections.singletonList(actionValue);

        for (final String effect : effects) {
            final String[] parts = effect.split(",", 6);
            if (parts.length < 5) continue;

            final Player target = getPlayerFromString(parts[0], player1, player2);
            if (target == null) continue;

            final PotionEffectType effectType = PotionEffectType.getByName(parts[1].toUpperCase());
            if (effectType == null) continue;

            final boolean b = parts.length == 6 && Boolean.parseBoolean(parts[5]);
            try {
                final PotionEffect potionEffect = new PotionEffect(effectType, Integer.parseInt(parts[2]) * 20, Integer.parseInt(parts[3]) - 1, Boolean.parseBoolean(parts[4]));
                TaskHandler.INSTANCE.runSync(() -> target.addPotionEffect(potionEffect, b));
            } catch (final NumberFormatException ignored) {
            }
        }
    }

    private void handlerRemoveEffect(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",", 2);
        if (parts.length != 2) return;

        final Player target = getPlayerFromString(parts[0], player1, player2);
        if (target == null) return;

        final PotionEffectType effectType = PotionEffectType.getByName(parts[1].toUpperCase());
        if (effectType == null) return;

        try {
            TaskHandler.INSTANCE.runSync(() -> target.removePotionEffect(effectType));
        } catch (final NumberFormatException ignored) {
        }
    }

    private void handlePlaySound(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",", 4);
        if (parts.length != 4) return;

        final Player target = getPlayerFromString(parts[0], player1, player2);
        if (target == null) return;

        final Sound sound;
        try {
            sound = Sound.valueOf(parts[1].toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return;
        }

        try {
            target.playSound(target.getLocation(), sound, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (final NumberFormatException ignored) {
        }
    }

    private void handlePlaySoundGlobally(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",", 4);
        if (parts.length != 4) return;

        final Player target = getPlayerFromString(parts[0], player1, player2);
        if (target == null) return;

        final Sound sound;
        try {
            sound = Sound.valueOf(parts[1].toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return;
        }

        try {
            final Location location = target.getLocation();

            final int volume = Integer.parseInt(parts[2]),
                    pitch = Integer.parseInt(parts[3]);

            for (final Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(location, sound, volume, pitch);
            }
        } catch (final NumberFormatException ignored) {
        }
    }

    private void handleSetVariableAction(final String actionValue, final Player player1, final Player player2) {
        final String[] parts = actionValue.split(",", 2);
        if (parts.length != 2) return;

        final String variableName = parts[0],
                value = parts[1];

        if (variableName != null && value != null) {
            if (variableName.startsWith("[") && variableName.endsWith("]")) {
                final String[] varParts = variableName.substring(1, variableName.length() - 1).split("::");
                if (varParts.length == 2) {
                    final Player targetPlayer = getPlayerFromString(varParts[1], player1, player2);
                    if (targetPlayer != null) {
                        setUniqueVariable(targetPlayer, varParts[0], resolveVariable(value, targetPlayer));
                    }
                }
                return;
            }

            staticVariables.put(variableName, resolveVariable(value, player1));
        }
    }

    private Player getPlayerFromString(final String playerString, final Player player1, final Player player2) {
        switch (playerString.toLowerCase()) {
            case "attacker":
            case "player":
                return player1;
            case "victim":
                return player2;
            default:
                return null;
        }
    }

    private Player getPlayerFromEvent(final Object event) {
        return event instanceof Player ? (Player) event : event instanceof EntityDamageByEntityEvent ? (Player) ((EntityDamageByEntityEvent) event).getEntity() : event instanceof PlayerInteractEvent ? ((PlayerInteractEvent) event).getPlayer() : null;
    }

    private String translateColorCodes(final String text) {
        return text == null ? null : ChatColor.translateAlternateColorCodes('&', text);
    }

    private int parseInt(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignored) {
            return 1;
        }
    }

    private void removeItem(final Player player, int count, final Material material, final String name) {
        if (material == null) return;

        final Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(material);

        int found = 0;
        for (final ItemStack stack : ammo.values()) {
            found += stack.getAmount();
        }

        if (count > found) return;

        for (final int index : ammo.keySet()) {
            final ItemStack stack = ammo.get(index);

            if (name != null && !stack.getItemMeta().getDisplayName().equals(name)) continue;

            final int removed = Math.min(count, stack.getAmount());
            count -= removed;

            if (stack.getAmount() == removed) {
                player.getInventory().setItem(index, null);
            } else {
                stack.setAmount(stack.getAmount() - removed);
            }

            if (count <= 0) break;
        }

        player.updateInventory();
    }

    private String resolveVariable(final String value, final Player player) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String[] parts = value.substring(1, value.length() - 1).split("::");
            if (parts.length == 2) {
                final String varName = parts[0],
                        varType = parts[1];

                if (varType.equals("player") || varType.equals("victim") || varType.equals("attacker")) {
                    final Player targetPlayer = getPlayerFromString(varType, player, null);
                    if (targetPlayer != null) {
                        return getUniqueVariable(targetPlayer, varName).toString();
                    }
                }
            }
        }
        return staticVariables.getOrDefault(value, value).toString();
    }

    private Object getUniqueVariable(final Player player, final String varName) {
        return uniqueVariables
                .computeIfAbsent(player, k -> new ConcurrentHashMap<>())
                .getOrDefault(varName, "null");
    }

    private void setUniqueVariable(final Player player, final String varName, final Object value) {
        uniqueVariables
                .computeIfAbsent(player, k -> new ConcurrentHashMap<>())
                .put(varName, value);
    }
}