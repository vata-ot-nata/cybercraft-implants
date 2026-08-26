package cyberprank.implants;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Implants extends JavaPlugin implements Listener {

    ArrayList<Implant> implants = new ArrayList<>();
    private static final ConcurrentHashMap<Player, Boolean> nogiActive = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, Boolean> sandActive = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        new Sandevistian();
        new Nogi();
    }

    @Override
    public void onDisable() {

    }

    public Plugin getInstance() {
        return this;
    }

    public void putOn(Player player, ItemStack implant, int slot) { // 0 - ботинки, 3 - шлем

        ItemStack[] armor = player.getInventory().getArmorContents();

        if (armor[slot] == null) {

            ItemMeta meta = implant.getItemMeta();

            List<String> lore;

            if (meta.hasLore()) {
                lore = meta.getLore();
            } else {
                lore = new ArrayList<>();
            }

            lore.add("");
            lore.add("§8[ОДЕТ]");
            meta.setLore(lore);
            implant.setItemMeta(meta);
            armor[slot] = implant;
            player.getInventory().setArmorContents(armor);

            player.getInventory().setItemInMainHand(null);
            player.sendMessage("Вы активировали имплант");
        }

    }

    public void use(Player player, ItemStack implant, ImplantActive implantActive, int cooldown) {
        if (player.hasCooldown(implant.getType())) {
            player.sendMessage("Подождите ещё " + (player.getCooldown(implant.getType())/20) + " секунд");
        } else {
            player.setCooldown(implant.getType(), cooldown);
            implantActive.activate(player, implant);
        }
    }

    public boolean isImplant(String name) {
        for (Implant implant : implants) {
            for (String implantname : implant.name) {
                if (Objects.equals(implantname, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Implant getImplant(String name) {
        for (Implant implant : implants) {
            for (String implantname : implant.name) {
                if (Objects.equals(implantname, name)) {
                    return implant;
                }
            }
        }
        return null;
    }

    public class Implant {
        private final String[] name;
        private final int slot;
        private final int cooldown;
        private final ImplantActive implantActive;

        public Implant(String[] name, int slot, int cooldown, ImplantActive implantActive) {
            this.name = name;
            this.slot = slot;
            this.cooldown = cooldown;
            this.implantActive = implantActive;

        }

        public String[] getName() {
            return this.name;
        }

        public int getSlot() {
            return this.slot;
        }

        public int getCooldown() {
            return this.cooldown;
        }

        public ImplantActive getImplantActive() {
            return this.implantActive;
        }
    }

    public abstract class ImplantActive {
        abstract void activate(Player player, ItemStack implant);
    }


    // IMPLANTS
    public class Sandevistian extends ImplantActive {

        public Sandevistian() {

            implants.add(new Implant(new String[]{"Sandevistian", "Sandevistian (Gradient)"}, 2, 100, this));
        }

        @Override
        void activate(Player player, ItemStack implant) {
            if (!sandActive.get(player)) {
                String name = ChatColor.stripColor(implant.getItemMeta().getDisplayName());
                sandActive.put(player, true);

                final int step;
                if (name.equals("Sandevistian (Gradient)")) {
                    step = 1;
                } else {
                    step = 360 / 100;
                }

                BukkitRunnable task = new BukkitRunnable() {
                    Location lastloc = player.getLocation();

                    private int globalCount = 0;
                    private int count = 0;
                    private int countstep = 1;

                    @Override
                    public void run() {
                        try {

                            boolean isWear = false;
                            for (ItemStack armor : player.getInventory().getArmorContents()) {
                                if (armor != null) {
                                    String armorName = ChatColor.stripColor(armor.getItemMeta().getDisplayName());
                                    if (armorName.equals("Sandevistian (Gradient)") || armorName.equals("Sandevistian")) {
                                        isWear = true;
                                    }
                                }
                            }
                            if (!isWear) {
                                player.removePotionEffect(PotionEffectType.SPEED);
                                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                                player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                                player.removePotionEffect(PotionEffectType.JUMP);
                                this.cancel();
                            }

                            if (!sandActive.get(player)) {
                                player.removePotionEffect(PotionEffectType.SPEED);
                                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                                player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                                player.removePotionEffect(PotionEffectType.JUMP);
                                this.cancel();
                            }

                            if (globalCount % 100 == 0) {
                                if (player.getHealth() <= 6 || player.getHealth() <= 2) {
                                    player.sendMessage("§2§lПредупреждение! §7Вы слишком много держите имплант включённым," +
                                            " это может перенагрузить ваше тело. Что бы такое происходло не слишком часто," +
                                            " советуем преобрести имуноблокаторные припораты");
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 600, 0));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3));

                                    sandActive.put(player, false);
                                    player.removePotionEffect(PotionEffectType.SPEED);
                                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                                    this.cancel();
                                } else {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 24));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 110, 1));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 110, 6));
                                    if (!nogiActive.get(player)) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 110, 1));
                                    }
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        if (p.getLocation().distance(player.getLocation()) <= 60 && player != p) {
                                            if (!sandActive.get(p)) {
                                                if (!p.hasPotionEffect(PotionEffectType.SLOW)) {
                                                    p.sendTitle("§2Вы замедлелись!", "§aКто-то рядом использует Sandevistian");
                                                }
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 5));
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 110, 5));
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 110, 3));
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 110, 129));
                                            }
                                        }
                                    }
                                    if (globalCount != 0) {
                                        player.damage(6);
                                    }
                                }
                            }

                            globalCount++;
                            count += countstep;
                            if (count >= 100) {
                                countstep = -1;
                            } else if (count <= 0) {
                                countstep = 1;
                            }

                            Location loc = player.getLocation();
                            loc.add(0, 1, 0);
                            if (!(loc.getX() == lastloc.getX()
                                    && loc.getY() == lastloc.getY()
                                    && loc.getZ() == lastloc.getZ())) {

                                int r, g, b;

                                if (name.equals("Sandevistian (Gradient)")) {
                                    r = (int) (2.55*count);
                                    g = (int) (191-(1.91*count));
                                    b = 255;
                                } else {
                                    int hue = count * step;
                                    if (hue < 60) {
                                        r = 255;
                                        g = (int)(255 * (hue / 60.0));
                                        b = 0;
                                    } else if (hue < 120) {
                                        r = (int)(255 * ((120 - hue) / 60.0));
                                        g = 255;
                                        b = 0;
                                    } else if (hue < 180) {
                                        r = 0;
                                        g = 255;
                                        b = (int)(255 * ((hue - 120) / 60.0));
                                    } else if (hue < 240) {
                                        r = 0;
                                        g = (int)(255 * ((240 - hue) / 60.0));
                                        b = 255;
                                    } else if (hue < 300) {
                                        r = (int)(255 * ((hue - 240) / 60.0));
                                        g = 0;
                                        b = 255;
                                    } else {
                                        r = 255;
                                        g = 0;
                                        b = (int)(255 * ((360 - hue) / 60.0));
                                    }
                                }

                                Particle.DustOptions dust = new Particle.DustOptions(
                                        Color.fromRGB(r, g, b), 1);

                                for (double x = -0.375; x <= 0.375; x += Math.random()*0.15+0.25) {
                                    for (double y = 0; y <= 1.7; y += Math.random()*0.2+0.425) {
                                        for (double z = -0.375; z <= 0.375; z += Math.random()*0.15+0.25) {
                                            Float yaw = player.getLocation().getYaw();
                                            Vector vector = new Vector();
                                            vector.setX(x);
                                            vector.setY(y);
                                            vector.setZ(z);
                                            vector.rotateAroundY(yaw);
                                            Location partloc = player.getLocation().clone().add(vector);
                                            loc.getWorld().spawnParticle(Particle.REDSTONE, partloc.getX(), partloc.getY(), partloc.getZ(), 0, 0, 0, 0, dust);
                                        }
                                    }
                                }
                            }
                            lastloc = loc;
                        } catch (Exception ex) {
                            sandActive.put(player, false);
                            player.removePotionEffect(PotionEffectType.SPEED);
                            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                            this.cancel();
                        }
                    }};

                task.runTaskTimer(getInstance(), 1, 1);

            } else {
                sandActive.put(player, false);
            }
        }
    }


    public class Nogi extends ImplantActive {

        public Nogi() {

            implants.add(new Implant(new String[]{"Leg Implant", "Leg Implant (Gradient)"}, 1, 100, this));
        }

        @Override
        void activate(Player player, ItemStack implant) {
                if (!nogiActive.get(player)) {
                    nogiActive.put(player, true);
                    player.setWalkSpeed(0.5F);

                    canJump.put(player, true);

                    BukkitRunnable task = new BukkitRunnable() {
                        int jumpCount = 0;
                        int count = 0;

                        @Override
                        public void run() {
                            try {
                                count++;

                                boolean isWear = false;
                                for (ItemStack armor : player.getInventory().getArmorContents()) {
                                    if (armor != null) {
                                        String armorName = ChatColor.stripColor(armor.getItemMeta().getDisplayName());
                                        if (armorName.equals("Leg Implant") || armorName.equals("Leg Implant (Gradient)")) {
                                            isWear = true;
                                        }
                                    }
                                }
                                if (!isWear) {
                                    nogiActive.put(player, false);
                                    player.setWalkSpeed(0.2F);
                                    player.removePotionEffect(PotionEffectType.JUMP);
                                    player.sendActionBar("");
                                    this.cancel();
                                }

                                if (player.getHealth() < 2 || (count >= 350 && player.getHealth() <= 6)) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 600, 0));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3));

                                    nogiActive.put(player, false);
                                    player.setWalkSpeed(0.2F);
                                    player.removePotionEffect(PotionEffectType.JUMP);
                                    player.sendActionBar("");
                                    this.cancel();
                                } else if (count >= 360) {
                                    player.sendMessage("§2§lПредупреждение! §7Вы слишком много держите имплант включённым," +
                                            " это может перенагрузить ваше тело. Что бы такое происходло не слишком часто," +
                                            " советуем преобрести имуноблокаторные припораты");
                                    player.damage(6);
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 5));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 600, 0));

                                    nogiActive.put(player, false);
                                    player.setWalkSpeed(0.2F);
                                    player.removePotionEffect(PotionEffectType.JUMP);
                                    player.sendActionBar("");
                                    this.cancel();
                                }

                                if (jumpCount > 0) {
                                    jumpCount--;
                                    String str = "§2||||||||||||||||||||||||||||||||||";
                                    int index = 33 - jumpCount + 2;
                                    str = str.substring(0, index) + "|§7" + str.substring(index + 1);
                                    player.sendActionBar(str);

                                    if (jumpCount == 0) {
                                        canJump.put(player, true);
                                    }
                                }
                                if (jumpCount == 0) {
                                    if (canJump.get(player)) {
                                        player.sendActionBar("§aВы можете прыгать");
                                    } else {
                                        jumpCount = 33;
                                    }
                                }

                                if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 2));
                                }
                                if (!nogiActive.get(player)) {
                                    player.setWalkSpeed(0.2F);
                                    player.removePotionEffect(PotionEffectType.JUMP);
                                    player.sendActionBar("");
                                    this.cancel();
                                }
                            } catch (Exception ex) {
                                nogiActive.put(player, false);
                                player.setWalkSpeed(0.2F);
                                player.removePotionEffect(PotionEffectType.JUMP);
                                player.sendActionBar("");
                                this.cancel();
                            }
                        }};

                    task.runTaskTimer(getInstance(), 1, 10);
                } else {
                    nogiActive.put(player, false);
                }
        }
    }

    // EVENTS
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        try {

            Player player = event.getPlayer();
            Action action = event.getAction();
            ItemStack item = event.getItem();
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

            if (action.isRightClick()) {
                if (isImplant(name)) {

                    event.setCancelled(true);

                    Implant implant = getImplant(name);
                    putOn(player, event.getItem(), implant.slot); // слот импланта
                }
            }

        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        try {

            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

            if (item.getItemMeta().getLore().contains("§8[ОДЕТ]")) {
                if (isImplant(name)) {

                    event.setCancelled(true);
                    player.closeInventory();

                    Implant implant = getImplant(name);
                    use(player, item, implant.implantActive, implant.cooldown); // кулдаун импланта
                }
            }
        } catch (Exception ignored) {}

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setWalkSpeed(0.2F);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        nogiActive.put(player, false);
        sandActive.put(player, false);
        canJump.put(player, true);
    }

    private static final ConcurrentHashMap<Player, Boolean> canJump = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() && nogiActive.get(player) && canJump.get(player)) {

            Vector v = player.getLocation().getDirection();
            v.setY(0).normalize().multiply(4).setY(1.5);
            player.setVelocity(v);

            player.sendActionBar("§7||||||||||||||||||||||||||||||||||");
            canJump.put(player, false);

        }
    }
}
