package pebbleprojects.strongMCPvP.functions.perks;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pebbleprojects.strongMCPvP.databaseData.PerkSlots;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.BlocksHandler;
import pebbleprojects.strongMCPvP.handlers.MathHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Perk {

    private static final Random random = new Random();
    private static final Pattern variablesPattern = Pattern.compile("(\\[.*?::.*?])|(\\b[a-zA-Z_][a-zA-Z0-9_]*\\b)");

    private final int id, price;
    private final boolean shared;
    private final ItemStack guiItem;
    private final Configuration config;
    private final Map<String, Object> staticVariables;
    private final Map<Player, Map<String, Object>> uniqueVariables;

    public Perk(final int id, final int price, final ItemStack guiItem, final Configuration perkSection) {
        this.id = id;
        this.price = price;
        this.guiItem = guiItem;
        this.config = perkSection;
        this.shared = perkSection.getBoolean("shared");

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

    public void onEntityDamage(final Player victim, final Player attacker, final EntityDamageEvent event) {
        if (hasPerk(attacker))
            executeSection(event.getFinalDamage() >= victim.getHealth() ? "kill" : "damage", attacker, victim, event);
    }

    public void onPlayerSpawn(final Player player) {
        executeSection("spawn", player, null, player);
    }

    public void onPlayerDeath(final Player victim, final Player attacker) {
        if (hasPerk(attacker))
            executeSection("death", victim, attacker, victim);
    }

    public void onPlayerClick(final PlayerInteractEvent event) {
        executeSection("click", event.getPlayer(), null, event);
    }

    public void onBlockPlace(final BlockPlaceEvent event) {
        executeSection("place", event.getPlayer(), null, event);
    }

    private boolean hasPerk(final Player attacker) {
        if (shared || attacker == null) return true;

        for (final PerkSlot perkSlot : PerkSlots.INSTANCE.get(attacker.getUniqueId()))
            if (id == perkSlot.getPerkId())
                return true;

        return false;
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

            if (key.toLowerCase().startsWith("chance of ")) {
                handleChanceOfActions(section, key, player1, player2, event);
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

    private void handleChanceOfActions(final Configuration section, final String chanceKey, final Player player1, final Player player2, final Object event) {
        final String chanceString = chanceKey.substring(10);
        if (!chanceString.endsWith("%")) return;

        final double chance;
        try {
            chance = Double.parseDouble(chanceString.substring(0, chanceString.length() - 1));
        } catch (final NumberFormatException ignored) {
            return;
        }

        if (chance < 0 || chance > 100) return;

        if (random.nextInt(100) <= chance) {
            executeActionsWithConditions(section.getSection(chanceKey), player1, player2, event, true);
            return;
        }

        final Configuration elseSection = section.getSection("else");
        if (elseSection != null)
            executeActionsWithConditions(elseSection, player1, player2, event, true);
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

                final String key = resolveVariables(parts[0].trim(), event),
                        value = resolveVariables(parts[1].trim(), event);

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
            case "hasItem":
                return hasItem(value, event);
            case "selfHit":
                return event instanceof EntityDamageByEntityEvent &&
                        ((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile &&
                        (Boolean.parseBoolean(value) == ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter().equals(((EntityDamageByEntityEvent) event).getEntity()));
            case "block":
                return event instanceof BlockPlaceEvent &&
                        ((BlockPlaceEvent) event).getBlockPlaced().getType() == Material.getMaterial(value);
            default:
                return key.equals(value);
        }
    }

    private boolean hasItem(final String value, final Object event) {
        final String[] parts = value.split(",");
        if (parts.length < 2) return false;

        final Player player = event instanceof Player ? (Player) event : getPlayerFromEvent(event, parts[0]);
        if (player == null) return false;

        final Material material = Material.getMaterial(parts[1]);
        if (material == null) return false;

        final String name = translateColorCodes(parts.length > 4 ? parts[4] : null);
        final boolean exact = parts.length > 3 && Boolean.parseBoolean(parts[3]);
        final int count = parts.length > 2 ? Math.min(parseInt(parts[2]), 1) : 1;

        final Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(material);

        int found = 0;
        for (final ItemStack stack : ammo.values()) {
            if (name != null) {
                final ItemMeta itemMeta = stack.getItemMeta();
                if (itemMeta == null || itemMeta.getDisplayName() == null || !itemMeta.getDisplayName().equals(name)) continue;
            }

            found += stack.getAmount();

            if (found > count) return !exact;
        }

        return found == count;
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
            case "setBlock":
                handleSetBlock(actionValue, player1, player2, event);
                break;
            case "setDamage":
                if (event instanceof EntityDamageEvent) {
                    handleSetDamage(actionValue, (EntityDamageEvent) event);
                }
                break;
            case "cancel-event":
                if (Boolean.parseBoolean(actionValue) && event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                }
                break;
        }
    }

    private void handleSetBlock(final String actionValue, final Player player1, final Player player2, final Object event) {
        final String[] parts = actionValue.split(",", 2);
        if (parts.length != 2) return;

        final Location location = getLocationFromString(parts[0], player1, player2, event);
        if (location == null) return;

        final Material material = Material.getMaterial(parts[1]);
        if (material != null) {
            TaskHandler.INSTANCE.runSync(() -> {
                location.getBlock().setType(material);
                if (material == Material.AIR) {
                    BlocksHandler.INSTANCE.removeFromCache(location);
                    return;
                }

                BlocksHandler.INSTANCE.addToCache(location);
            });
        }
    }

    private void handleSetDamage(final String actionValue, final EntityDamageEvent event) {
        try {
            event.setDamage(Double.parseDouble(MathHandler.INSTANCE.processString(resolveVariables(actionValue.replace("damage", String.valueOf(event.getDamage())), event))));
        } catch (final NumberFormatException ignored) {}
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
        if (target != null)
            target.sendMessage(translateColorCodes(resolveVariables(parts[1], target)));
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
                        setUniqueVariable(targetPlayer, varParts[0], resolveVariables(value, targetPlayer));
                    }
                }
                return;
            }

            staticVariables.put(variableName, resolveVariables(value, player1));
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

    private Location getLocationFromString(final String locationString, final Player player1, final Player player2, final Object event) {
        switch (locationString.toLowerCase()) {
            case "player's location":
            case "attacker's location":
            case "location of player":
            case "location of attacker":
                return player1.getLocation();
            case "victim's location":
            case "location of victim":
                return player2.getLocation();
            case "block's location":
            case "location of block":
                return event instanceof BlockPlaceEvent ? ((BlockPlaceEvent) event).getBlockPlaced().getLocation() : null;
            case "click's location":
            case "location of click":
                return event instanceof PlayerInteractEvent ? ((PlayerInteractEvent) event).getClickedBlock().getLocation().clone().add(0, 1, 0) : null;
            default:
                return null;
        }
    }

    private Player getPlayerFromEvent(final Object event, final String type) {
        return event instanceof Player ? (Player) event : event instanceof EntityDamageByEntityEvent ? type.equals("attacker") || type.equals("player") ? ((EntityDamageByEntityEvent) event).getDamager() instanceof Player ? (Player) ((EntityDamageByEntityEvent) event).getDamager() : null : type.equals("victim") ? (Player) ((EntityDamageByEntityEvent) event).getEntity() : null : event instanceof PlayerInteractEvent ? ((PlayerInteractEvent) event).getPlayer() : null;
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
        for (final ItemStack stack : ammo.values())
            found += stack.getAmount();

        if (count > found) return;

        for (final int index : ammo.keySet()) {
            final ItemStack stack = ammo.get(index);

            if (name != null && (stack == null || stack.getItemMeta() == null || stack.getItemMeta().getDisplayName() == null)) continue;

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

    private String resolveVariables(final String input, final Object event) {
        String result = input;
        final Matcher matcher = variablesPattern.matcher(input);

        while (matcher.find()) {
            final String variable = matcher.group();
            if (!variable.matches("\\d+")) {
                result = result.replace(variable, variable.startsWith("[") ?
                        resolveVariable(variable, event) :
                        staticVariables.getOrDefault(variable, variable).toString());
            }
        }

        return MathHandler.INSTANCE.processString(result);
    }

    private String resolveVariables(final String input, final Player player) {
        String result = input;
        final Matcher matcher = variablesPattern.matcher(input);

        while (matcher.find()) {
            final String variable = matcher.group();
            if (!variable.matches("\\d+")) {
                result = result.replace(variable, variable.startsWith("[") ?
                        resolveVariable(variable, player) :
                        staticVariables.getOrDefault(variable, variable).toString());
            }
        }

        return MathHandler.INSTANCE.processString(result);
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

    private String resolveVariable(final String value, final Object event) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String[] parts = value.substring(1, value.length() - 1).split("::");
            if (parts.length == 2) {
                final String varName = parts[0],
                        varType = parts[1];

                if (varType.equals("player") || varType.equals("victim") || varType.equals("attacker")) {
                    final Player targetPlayer = getPlayerFromEvent(event, varType);
                    if (targetPlayer != null) {
                        return getUniqueVariable(targetPlayer, varName).toString();
                    }
                }
            }
        }

        return staticVariables.getOrDefault(value, value).toString();
    }

    private Object getUniqueVariable(final Player player, final String varName) {
        return uniqueVariables.getOrDefault(player, new ConcurrentHashMap<>()).getOrDefault(varName, "null");
    }

    private void setUniqueVariable(final Player player, final String varName, final Object value) {
        final Map<String, Object> map = uniqueVariables.getOrDefault(player, new ConcurrentHashMap<>());
        map.put(varName, value);

        uniqueVariables.put(player, map);
    }
}