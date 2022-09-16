package me.dreig_michihi.meteordash;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.CombustionAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class MeteorDash extends CombustionAbility implements AddonAbility {
    private Listener MDL;
    private Location location;
    private double maxHeightAfterExplosion;
    private Location right;
    private Location left;

    @Attribute("ExplosionsCount")
    public int count;//count of explosions
    @Attribute("MeteorDamage")
    private double meteorDamage;
    @Attribute(Attribute.KNOCKUP)
    private double userKnockback;
    @Attribute(Attribute.KNOCKBACK)
    private double enemiesKnockback;
    @Attribute(Attribute.CHARGE_DURATION)
    private Long timeBetweenExplosions;
    @Attribute(Attribute.COOLDOWN)
    private Long cooldown;
    @Attribute(Attribute.DAMAGE)
    private double auraDamage;
    @Attribute(Attribute.FIRE_TICK)
    private int auraFireTicks;
    private boolean meteoriteSetsCooldown;
    @Attribute(Attribute.HEIGHT)
    private double minHeightToMeteorite;
    @Attribute(Attribute.SELECT_RANGE)
    private double tooCloseDistation;
    private Long minTimeBetweenExplosions;
    @Attribute(Attribute.SPEED)
    private double doAuraSpeed;
    private double doMeteoriteSpeed;
    private boolean fireInHands;
    private boolean fireAuraOnlyWhenAbilityIsChoosed;
    private double fireAuraRadius;

    ArrayList<Entity> hitEntities;
    private Long lastBlastedTime = null;
    private Permission perm;
    private boolean Charged = false;
    static String path = "ExtraAbilities.Dreig_Michihi.Fire.MeteorDash.";


    public MeteorDash(Player player) {
        super(player);

        if (bPlayer.isOnCooldown(this)) {
            return;
        }
        if (!bPlayer.canBend(this)) {
            return;
        }
        if (hasAbility(player, getAbility("FireJet").getClass())) {
            return;
        }
        if (hasAbility(player, MeteorDash.class)) {
            MeteorDash md = getAbility(player, MeteorDash.class);
            if (md.isStarted())
                return;
        }
        //this.count = ;
        //player.sendMessage("\nКонструктор число взрывов установлено как: "+count);
        setFields();

        start();
    }
    private void setFields() {

        count = ConfigManager.defaultConfig.get().getInt(path + "ExplosionsCount");
        meteorDamage = ConfigManager.defaultConfig.get().getDouble(path + "MeteorDamage");
        auraDamage = ConfigManager.defaultConfig.get().getDouble(path + "AuraDamage");
        auraFireTicks = ConfigManager.defaultConfig.get().getInt(path + "AuraFireTicks");
        userKnockback = ConfigManager.defaultConfig.get().getDouble(path + "UserKnockback");
        enemiesKnockback = ConfigManager.defaultConfig.get().getDouble(path + "EnemiesKnockback");
        timeBetweenExplosions = ConfigManager.defaultConfig.get().getLong(path + "TimeBetweenExplosions");
        cooldown = ConfigManager.defaultConfig.get().getLong(path + "Cooldown");
        meteoriteSetsCooldown = ConfigManager.defaultConfig.get().getBoolean(path + "MeteoriteSetsCooldown");
        minHeightToMeteorite = ConfigManager.defaultConfig.get().getDouble(path + "MinHeightToMeteorite");
        tooCloseDistation = ConfigManager.defaultConfig.get().getDouble(path + "TooCloseDistation");
        doAuraSpeed = ConfigManager.defaultConfig.get().getDouble(path + "DoAuraSpeed");
        doMeteoriteSpeed = ConfigManager.defaultConfig.get().getDouble(path + "DoMeteoriteSpeed");
        minTimeBetweenExplosions = ConfigManager.defaultConfig.get().getLong(path + "MinTimeBetweenExplosions");
        fireInHands = ConfigManager.defaultConfig.get().getBoolean(path + "FireInHands");
        fireAuraOnlyWhenAbilityIsChoosed = ConfigManager.defaultConfig.get().getBoolean(path+"FireAuraOnlyWhenAbilityIsChoosed");
        fireAuraRadius = ConfigManager.defaultConfig.get().getDouble(path+"FireAuraRadius");
        maxHeightAfterExplosion = player.getLocation().getY();
        hitEntities = new ArrayList<>();
        applyModifiers(meteorDamage);
        //location = player.getLocation();
    }

    private void applyModifiers(double damage) {
        int damageMod;

        damageMod = (int) (this.getDayFactor(damage) - damage);

        damageMod = (int) (bPlayer.canUseSubElement(Element.SubElement.BLUE_FIRE) ? (BlueFireAbility.getDamageFactor() * damage - damage) + damageMod : damageMod);

        this.meteorDamage += damageMod;
        this.auraDamage +=damageMod;
    }

    boolean bigSpeed = false;
    private double speed;

    private void fireAura(Player player) {
        Random random = new Random();
        speed = player.getVelocity().length() * 20;
        bigSpeed = speed > doMeteoriteSpeed;
        double speedParticles = (int) speed;
        if (speedParticles > 40) speedParticles = 40;
        if(fireInHands) {
            right = player.getLocation().add(0, 0.65, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getRightHeadDirection(player).multiply(0.6D));
            left = player.getLocation().add(0, 0.65, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getLeftHeadDirection(player).multiply(0.6D));
        }
        if(fireInHands&&speed>10&&bPlayer.getBoundAbilityName().equalsIgnoreCase("MeteorDash")) {
            if (!isWater(left.getBlock()))
                if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                    ParticleEffect.SOUL_FIRE_FLAME.display(left, (int) speedParticles, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, speedParticles /1000);
                else
                    ParticleEffect.FLAME.display(left, (int) speedParticles, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, speedParticles /1000);
            else ParticleEffect.WATER_BUBBLE.display(left, 15, 0.25, 0.25, 0.25, 0.5);
            if (!isWater(right.getBlock()))
                if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                    ParticleEffect.SOUL_FIRE_FLAME.display(right, (int) speedParticles, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, speedParticles /1000);
                else
                    ParticleEffect.FLAME.display(right, (int) speedParticles, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, speedParticles /1000);
            else ParticleEffect.WATER_BUBBLE.display(right, 15, 0.25, 0.25, 0.25, 0.5);

            if (random.nextDouble() < 0.5) {
                playFirebendingSound(left);
                playFirebendingSound(right);
            }
        }
        if (speed > doAuraSpeed) {
            //player.setGliding(true);

            Vector pMoveDirection = player.getVelocity().normalize();
            Location auraLocation = player.getLocation().add(0, 0.9, 0).add(pMoveDirection.multiply(2));
            double auraRadius = 2D;
            player.setFallDistance(0);
            if(fireInHands) {
                if (bigSpeed)
                    ParticleEffect.SMOKE_NORMAL.display(left, 10, 0.01, 0.01, 0.01, 0.7);
                if (!isWater(left.getBlock()))
                    playFirebendingParticles(left, (int) speedParticles, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10);
                else ParticleEffect.BUBBLE_POP.display(left, 15, 0.25, 0.25, 0.25);

                if (bigSpeed)
                    ParticleEffect.SMOKE_NORMAL.display(right, 10, 0.01, 0.01, 0.01, 0.7);
                if (!isWater(right.getBlock()))
                    playFirebendingParticles(right, (int) speedParticles, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10, (speedParticles /1000)* speedParticles /10);
                else ParticleEffect.BUBBLE_POP.display(right, 15, 0.25, 0.25, 0.25);

                if (random.nextDouble() < 0.5) {
                    playFirebendingSound(player.getEyeLocation());
                }
                if(bigSpeed)
                    player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_DEATH, 2, 0.5F);
                else
                    player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_DEATH, 1, 0.1F);
            }
            else {
                for (double theta = 0.0D; theta < 180.0D; theta += 20D) {
                    for (double phi = 0.0D; phi < 360.0D; phi += 20D) {
                        double rphi = Math.toRadians(phi);
                        double rtheta = Math.toRadians(theta);
                        Location display = auraLocation.clone().add(auraRadius / 1.5D * Math.cos(rphi) * Math.sin(rtheta), auraRadius / 1.5D * Math.cos(rtheta), auraRadius / 1.5D * Math.sin(rphi) * Math.sin(rtheta));

                        if (random.nextDouble() < 0.20) {
                            if (!isWater(auraLocation.getBlock())) {
                                if (bPlayer.canUseSubElement(Element.BLUE_FIRE)) {
                                    if (bigSpeed && random.nextDouble() < 0.10)
                                        ParticleEffect.SMOKE_NORMAL.display(display, 10, 0.2, 0.2, 0.2, 0.3);
                                    ParticleEffect.SOUL_FIRE_FLAME.display(display, 1, 0.1, 0.1, 0.1, 0.5);
                                } else {
                                    if (bigSpeed && random.nextDouble() < 0.10)
                                        ParticleEffect.SMOKE_NORMAL.display(display, 10, 0.2, 0.2, 0.2, 0.3);
                                    ParticleEffect.FLAME.display(display, 1, 0.1, 0.1, 0.1, 0.5);
                                }
                            } else {
                                ParticleEffect.WATER_BUBBLE.display(display, 10, 0.1, 0.1, 0.1, 0.7);
                            }
                        }

                        if (random.nextDouble() < 0.01) {
                            playFirebendingSound(display);
                        }
                    }
                }
            }
            for (Entity entity : GeneralMethods.getEntitiesAroundPoint(auraLocation, fireAuraRadius)) {
                if ((entity instanceof LivingEntity) && entity.getUniqueId() != player.getUniqueId() && !hitEntities.contains(entity)) {
                    DamageHandler.damageEntity(entity, auraDamage, this);
                    entity.setFireTicks(auraFireTicks);
                    ParticleEffect.EXPLOSION_HUGE.display(((LivingEntity) entity).getEyeLocation(),1,0.3,0.3,0.3);
                    ParticleEffect.EXPLOSION_NORMAL.display(((LivingEntity) entity).getEyeLocation(),10,0.3,0.3,0.3, 0.6);
                    Vector dir = GeneralMethods.getDirection(player.getEyeLocation(), ((LivingEntity) entity).getEyeLocation()).normalize().multiply(enemiesKnockback);
                    entity.setVelocity(dir);
                    hitEntities.add(entity);
                    player.getWorld().playSound(((LivingEntity) entity).getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 2.0F);
                }
            }
        }
    }

    private void meteorite(Location craterLoc) {
        craterLoc.add(0, 2, 0);
        ParticleEffect.EXPLOSION_LARGE.display(craterLoc, 5, 1, 1, 1, 1);
        ParticleEffect.EXPLOSION_NORMAL.display(craterLoc, 10, 1, 1, 1, 1);
        if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
            ParticleEffect.SOUL_FIRE_FLAME.display(craterLoc, 50, 3, 3, 3, 1);
        else
            ParticleEffect.FLAME.display(craterLoc, 50, 3, 3, 3, 1);
        for (Entity e : GeneralMethods.getEntitiesAroundPoint(craterLoc, 5)) {
            if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
                Vector direction = GeneralMethods.getDirection(craterLoc, ((LivingEntity) e).getEyeLocation()).normalize().multiply(enemiesKnockback);
                DamageHandler.damageEntity(e, meteorDamage, this);
                e.setVelocity(direction);
            }
        }
    }

    private void explosion(Location explodeCenter) {
        boolean explodeWater = isWater(explodeCenter.getBlock());
        Location right = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getRightHeadDirection(player).multiply(0.6D));
        Location left = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getLeftHeadDirection(player).multiply(0.6D));
        if (!explodeWater)
            if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                ParticleEffect.SOUL_FIRE_FLAME.display(explodeCenter, 10, 0.1, 0.1, 0.1, 0.5);
            else
                ParticleEffect.FLAME.display(explodeCenter, 10, 0.1, 0.1, 0.1, 0.5);
        if (!explodeWater)
            ParticleEffect.EXPLOSION_HUGE.display(explodeCenter, 1);
        else
            ParticleEffect.EXPLOSION_LARGE.display(explodeCenter, 1);
        if (!explodeWater)
            ParticleEffect.EXPLOSION_LARGE.display(left, 1);
        else
            ParticleEffect.EXPLOSION_NORMAL.display(explodeCenter, 1);
        if (!explodeWater)
            if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                ParticleEffect.SOUL_FIRE_FLAME.display(left, 5, 0.1, 0.1, 0.1, 0.1);
            else
                ParticleEffect.FLAME.display(left, 5, 0.1, 0.1, 0.1, 0.1);
        if (!explodeWater)
            ParticleEffect.EXPLOSION_LARGE.display(right, 1);
        else
            ParticleEffect.EXPLOSION_NORMAL.display(explodeCenter, 1);
        if (!explodeWater)
            if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                ParticleEffect.SOUL_FIRE_FLAME.display(right, 5, 0.1, 0.1, 0.1, 0.1);
            else
                ParticleEffect.FLAME.display(right, 5, 0.1, 0.1, 0.1, 0.1);
        player.getWorld().playSound(explodeCenter, Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 3.0F);
        for (Entity e : GeneralMethods.getEntitiesAroundPoint(explodeCenter, 4)) {
            if (e instanceof LivingEntity) {
                Vector direction;
                if (!explodeWater)
                    if (e.getUniqueId() == player.getUniqueId())
                        direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(userKnockback);
                    else
                        direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(enemiesKnockback);
                else if (e.getUniqueId() == player.getUniqueId())
                    direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(userKnockback / 2);
                else
                    direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(enemiesKnockback / 2);
                if (e.getUniqueId() != player.getUniqueId())
                    DamageHandler.damageEntity(e, meteorDamage, this);
                e.setVelocity(direction);
            }
        }
    }

    public Vector getRightHeadDirection(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        return (new Vector(-direction.getZ(), 0.0D, direction.getX())).normalize();
    }

    public Vector getLeftHeadDirection(Player player) {
        Vector direction = player.getLocation().clone().getDirection().normalize();
        return (new Vector(direction.getZ(), 0.0D, -direction.getX())).normalize();
    }

    public Vector getBackHeadDirection(Player player) {
        Vector direction = player.getLocation().clone().getDirection().normalize();
        return (new Vector(-direction.getX(), 0.0D, -direction.getZ())).normalize();
    }

    private void endMD(){
        bPlayer.addCooldown(this);
        remove();
    }
    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || bPlayer.isChiBlocked() || !bPlayer.isToggled()) {
            endMD();
            return;
        }
        if (lastBlastedTime != null) {
            if (!bPlayer.isOnCooldown("FireJet"))
                bPlayer.addCooldown(getAbility("FireJet"), 50);
            if (System.currentTimeMillis() > lastBlastedTime + timeBetweenExplosions) {
                endMD();
                return;
            }
        }
        if (player.getLocation().getY() >= maxHeightAfterExplosion)
            maxHeightAfterExplosion = player.getLocation().getY();
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 1.0D);
        double landingHeight = player.getLocation().getY();
        if (GeneralMethods.isSolid(loc.getBlock())) {
            if (speed > doAuraSpeed && maxHeightAfterExplosion - landingHeight > minHeightToMeteorite) {
                Location destination = player.getEyeLocation().add(1.5, 0.0D, 1.5);
                Vector vec = GeneralMethods.getDirection(this.player.getLocation(), destination.clone());
                for (int i = 0; i <= 360; i += 5) {
                    vec = GeneralMethods.rotateXZ(vec, i - 180);
                    vec.setY(0);
                    if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                        ParticleEffect.SOUL_FIRE_FLAME.display(player.getLocation().add(vec), 5, 0.1, 0.1, 0.1, 0.5);
                    else
                        ParticleEffect.FLAME.display(player.getLocation().add(vec), 5, 0.1, 0.1, 0.1, 0.5);

                }
                player.getWorld().playSound(destination, Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.5F);
            }
            if (bigSpeed && maxHeightAfterExplosion - landingHeight > minHeightToMeteorite) {
                meteorite(loc);
                if (meteoriteSetsCooldown) {
                    endMD();
                    return;
                }
            }
            if (count == 0 && System.currentTimeMillis() > lastBlastedTime + minTimeBetweenExplosions) {
                endMD();
                return;
            }
            maxHeightAfterExplosion = player.getLocation().getY();
        }
        if (lastBlastedTime != null) {
            if (fireAuraOnlyWhenAbilityIsChoosed) {
                if (bPlayer.getBoundAbilityName().equalsIgnoreCase("MeteorDash"))
                    fireAura(player);
            } else
                fireAura(player);
        }
        if (bPlayer.getBoundAbilityName().equalsIgnoreCase("MeteorDash")) {
            location = player.getEyeLocation();
            Vector direction = location.getDirection().normalize();
            boolean tooClose = false;
            for (double i = 0; i < tooCloseDistation; i += 0.4D) {
                if (GeneralMethods.isSolid(location.clone().add(direction.clone().multiply(i)).getBlock())) {
                    tooClose = true;
                    i = tooCloseDistation;
                }
            }
            if (count > 0 && !tooClose) {
                Vector directionBack = direction.clone().multiply(-1).normalize(); // противоположное направление
                Location explodeCenter = location.clone().add(directionBack); // центр взрыва
                right = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getRightHeadDirection(player).multiply(0.6D));
                left = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getLeftHeadDirection(player).multiply(0.6D));
                if (player.isSneaking()) {
                    if (lastBlastedTime == null || (System.currentTimeMillis() > lastBlastedTime + minTimeBetweenExplosions)) {
                        Charged = true;
                        ParticleEffect.CRIT.display(location.clone().add(direction.clone().multiply(tooCloseDistation).add(getRightHeadDirection(player).multiply(0.6D))), 1, 0.1, 0.1, 0.1, 0);
                        ParticleEffect.CRIT.display(location.clone().add(direction.clone().multiply(tooCloseDistation).add(getLeftHeadDirection(player).multiply(0.6D))), 1, 0.1, 0.1, 0.1, 0);
                        Random rand = new Random();
                        if (rand.nextDouble() < 0.15) {
                            ParticleEffect.SMOKE_LARGE.display(left, 1, 0.1, 0.1, 0.1, 0.03);
                            ParticleEffect.SMOKE_LARGE.display(right, 1, 0.1, 0.1, 0.1, 0.03);
                        }
                        ParticleEffect.SMOKE_NORMAL.display(left, 1, 0.1, 0.1, 0.1, 0.1);
                        ParticleEffect.SMOKE_NORMAL.display(right, 1, 0.1, 0.1, 0.1, 0.1);
                    }
                } else if (Charged) {
                    Charged = false;
                    count--;
                    hitEntities.clear();
                    explosion(explodeCenter);
                    lastBlastedTime = System.currentTimeMillis();
                }
            } else {
                Charged = false;
                if (lastBlastedTime == null)
                    remove();
            }
        } else {
            Charged = false;
            if (lastBlastedTime == null)
                remove();
        }
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getName() {
        return "MeteorDash";
    }

    @Override
    public String getDescription() {
        return ConfigManager.defaultConfig.get().getString(path + "Description");
    }

    @Override
    public String getInstructions() {
        return ConfigManager.defaultConfig.get().getString(path + "Usage");
    }

    @Override

    public String getAuthor() {
        return "" + ChatColor.DARK_RED + "Dreig_Michihi";
    }

    @Override
    public String getVersion() {
        return ChatColor.GOLD + "1.5.4";
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public void load() {
        this.MDL = new MeteorDashListener();
        Bukkit.getPluginManager().registerEvents(this.MDL, ProjectKorra.plugin);
        //ConfigManager.defaultConfig.get().addDefault(path + "ExplosionRadius", 4);
        ConfigManager.defaultConfig.get().addDefault(path + "ExplosionsCount", 3);
        ConfigManager.defaultConfig.get().addDefault(path + "MeteorDamage", 2);
        ConfigManager.defaultConfig.get().addDefault(path + "AuraDamage", 2);
        ConfigManager.defaultConfig.get().addDefault(path + "AuraFireTicks", 19);
        ConfigManager.defaultConfig.get().addDefault(path + "UserKnockback", 2.5);
        ConfigManager.defaultConfig.get().addDefault(path + "EnemiesKnockback", 1.5);
        ConfigManager.defaultConfig.get().addDefault(path + "TimeBetweenExplosions", 5000);
        ConfigManager.defaultConfig.get().addDefault(path + "Cooldown", 7000);
        ConfigManager.defaultConfig.get().addDefault(path + "MeteoriteSetsCooldown", true);
        ConfigManager.defaultConfig.get().addDefault(path + "MinHeightToMeteorite", 10);
        ConfigManager.defaultConfig.get().addDefault(path + "TooCloseDistation", 3.0);
        ConfigManager.defaultConfig.get().addDefault(path + "DoAuraSpeed", 20);
        ConfigManager.defaultConfig.get().addDefault(path + "DoMeteoriteSpeed", 35);
        ConfigManager.defaultConfig.get().addDefault(path + "MinTimeBetweenExplosions", 500);
        ConfigManager.defaultConfig.get().addDefault(path + "FireInHands", true);
        ConfigManager.defaultConfig.get().addDefault(path + "FireAuraOnlyWhenAbilityIsChoosed", false);
        ConfigManager.defaultConfig.get().addDefault(path + "FireAuraRadius", 2.0);
        ConfigManager.defaultConfig.get().addDefault(path + "Description", "" +
                "Having solved the secret of subordinating explosions to their will, " +
                "combustionbenders are able to create explosions from their limbs, pushing " +
                "themselves in the air over long distances. Accelerating to high speed, " +
                "they are able to create a fiery shell in front of them, like meteors (This shell " +
                "additionally protects the benders from falling damage). If using this technique " +
                "to fall from a big height at high speed, then combustionbender will create a strong " +
                "explosion around him, pushing everyone to the sides.");
        ConfigManager.defaultConfig.get().addDefault(path + "Usage", "" +
                "To blast yourself in the direction you look, " +
                "hold and release Sneak(Default Shift). For some time after the explosion," +
                " you can create another one and so on up to a certain limit." +
                " Falling from great heights at high speed will automatically" +
                " create a powerful explosion upon landing.");
        ConfigManager.defaultConfig.save();
        perm = new Permission("bending.ability.MeteorDash");
        perm.setDefault(PermissionDefault.TRUE);
        ProjectKorra.log.info(this.getName() + " by " + this.getAuthor() + " " + this.getVersion() + " has been loaded!");
    }

    @Override
    public void stop() {
        ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(this.perm);
        HandlerList.unregisterAll(this.MDL);
        super.remove();
    }
}