package com.enderitefox.dimensionalweather.weather;

import com.enderitefox.dimensionalweather.Config;
import com.enderitefox.dimensionalweather.DimensionalWeather;
import com.enderitefox.dimensionalweather.client.oblivion.OblivionChargeBar;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EventBusSubscriber(modid = DimensionalWeather.MODID)
public class OblivionWeather {
    public static boolean aboveVoid;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Level level = event.getEntity().level();

        if (!level.dimension().identifier().equals(BuiltinDimensionTypes.END.identifier())) {
            return;
        }

        tickOblivionCharge(event.getEntity(), level.isClientSide());

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            tickOblivionDamage(serverPlayer);
        }
    }

    public static void tickOblivionCharge(Player player, boolean clientSide) {
        aboveVoid = isAboveVoid(player);
        double charge = player.getData(DimensionalWeather.OBLIVION_CHARGE);
        double tickSpeedRatio = player.level().tickRateManager().tickrate() / 20.0;
        double deltaTime = (1.0 / 20.0) * tickSpeedRatio;
        if (aboveVoid) {
            charge += Config.OBLIVION_FILL_RATE.get() * deltaTime;
        }
        else {
            charge -= Config.OBLIVION_EMPTY_RATE.get() * deltaTime;
        }

        charge = Math.clamp(charge, 0.0, 1.0);

        player.setData(DimensionalWeather.OBLIVION_CHARGE, charge);

        if (clientSide) {
            OblivionChargeBar.currentVal = charge;
        }
    }

    public static void tickOblivionDamage(ServerPlayer player) {
        if (player.level().getServer().getTickCount() % 20 != 0) {
            return;
        }

        double charge = player.getData(DimensionalWeather.OBLIVION_CHARGE);
        if (charge == 1.0) {
            player.hurtServer(
                player.level(),
                new DamageSource(
                    player
                        .level()
                        .registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(DimensionalWeather.OBLIVION_DAMAGE),
                    null,
                    null,
                    player.getEyePosition().add(0, -10, 0)
                ),
                Config.OBLIVION_DAMAGE.get().floatValue()
            );
        }
    }

    public static boolean isAboveVoid(Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = player.getEyePosition().add(0, -DimensionDefaults.END_LOGICAL_HEIGHT, 0);
        BlockHitResult result = player.level().clip(
            new ClipContext(
                from,
                to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                player
            )
        );

        return result.getBlockPos().getY() < -64;
    }

    @SubscribeEvent
    public static void onOblivionDamage(LivingIncomingDamageEvent event) {
        if (event.getAmount() <= 0.0f) {
            return;
        }

        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!event.getSource().is(DimensionalWeather.OBLIVION_DAMAGE)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        DeflectingItem deflectingItem = getDeflectingItem(entity);
        if (deflectingItem == null) {
            return;
        }
        ItemStack item = deflectingItem.item;

        if (!item.isDamageableItem()) {
            return;
        }

        BlocksAttacks blocksAttacks = item.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks == null) {
            return;
        }

        LivingShieldBlockEvent ev = CommonHooks.onDamageBlock(entity, event.getContainer(), event.getAmount(), true);
        blocksAttacks.onBlocked(serverLevel, entity);
        blocksAttacks.hurtBlockingItem(serverLevel, item, entity, deflectingItem.hand, event.getAmount(), ev.shieldDamage());
        event.setCanceled(true);
    }

    private static DeflectingItem getDeflectingItem(LivingEntity entity) {
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();

        Holder<Enchantment> deflectEnchantHolder = entity.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(DimensionalWeather.DEFLECT_ENCHANTMENT);

        if (!mainHandItem.isEmpty() && mainHandItem.getEnchantmentLevel(deflectEnchantHolder) > 0) {
            return new DeflectingItem(mainHandItem, InteractionHand.MAIN_HAND);
        }
        else if (!offHandItem.isEmpty() && offHandItem.getEnchantmentLevel(deflectEnchantHolder) > 0) {
            return new DeflectingItem(offHandItem, InteractionHand.OFF_HAND);
        }

        return null;
    }

    private record DeflectingItem(ItemStack item, InteractionHand hand) {}
}
