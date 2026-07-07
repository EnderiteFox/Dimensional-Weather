package com.enderitefox.dimensionalweather.weather;

import com.enderitefox.dimensionalweather.Config;
import com.enderitefox.dimensionalweather.DimensionalWeather;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = DimensionalWeather.MODID)
class OblivionWeather {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Level level = event.getEntity().level();

        if (!level.dimension().identifier().equals(BuiltinDimensionTypes.END.identifier())) {
            return;
        }

        tickOblivionCharge(event.getEntity());

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            tickOblivionDamage(serverPlayer);
        }
    }

    public static void tickOblivionCharge(Player player) {
        boolean oblivionCharges = isAboveVoid(player);
        double charge = player.getData(DimensionalWeather.OBLIVION_CHARGE);
        double tickTime = player.level().tickRateManager().millisecondsPerTick() / 1000;
        if (oblivionCharges) {
            charge += Config.OBLIVION_FILL_RATE.get() * tickTime;
        }
        else {
            charge -= Config.OBLIVION_EMPTY_RATE.get() * tickTime;
        }

        charge = Math.clamp(charge, 0.0, 1.0);

        player.setData(DimensionalWeather.OBLIVION_CHARGE, charge);
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
                        .getOrThrow(DamageTypes.FELL_OUT_OF_WORLD),
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
}
