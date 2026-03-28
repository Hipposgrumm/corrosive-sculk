package dev.hipposgrumm.corrosive_sculk.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
//? if forge {
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;
//?} else {
/*import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
*///?}

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SculkDamagingEntitiesManager
    //? if forgebase {
    extends SimplePreparableReloadListener<Void>
    //?} else {
    /*implements ServerLifecycleEvents.EndDataPackReload
    *///?}
{
    private Map<ResourceLocation,Integer> SCULK_DAMAGING_ENTITIES = ImmutableMap.of();

    //? if forgebase {
    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
    //?} else {
    /*@Override
    public void endDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        if (!success) return;
    *///?}
        ImmutableMap.Builder<ResourceLocation, Integer> builder = ImmutableMap.builder();
        for (PackResources pack:resourceManager.listPacks().toList()) {
            for (String namespace:pack.getNamespaces(PackType.SERVER_DATA)) {
                try {
                    InputStream inputstream = null;
                    try {
                        // Find the file if it's there, otherwise it is skipped.
                        // If it's not there then don't throw an exception like otherwise would happen.
                        IoSupplier<InputStream> supplier = pack.getResource(PackType.SERVER_DATA,
                                //$ resourcelocation
                                ResourceLocation.fromNamespaceAndPath
                                        (namespace, "sculk_damaging_entities.json"));
                        if (supplier != null) inputstream = supplier.get();
                    } catch (IOException ignored) {}
                    if (inputstream != null) {
                        try {
                            Reader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8); // Not AutoClosable
                            try {
                                JsonObject jsonobject = GsonHelper.parse(reader);
                                for(String entry : jsonobject.keySet()) {
                                    //? if forgebase {
                                    ResourceLocation entity = ResourceLocation.parse(entry);
                                    //?} else {
                                    /*ResourceLocation entity = ResourceLocation.tryParse(entry);
                                    *///?}
                                    try {
                                                            // The number is multiplied by 2 because each heart is 2 health.
                                                            // Using getAsDouble() before subsequently casting to int is just user-proofing.
                                        builder.put(entity, (int) (jsonobject.get(entry).getAsDouble()*2));
                                    } catch (NumberFormatException ignored) {}
                                }
                            } catch (Throwable parseError) {
                                try {
                                    reader.close();
                                } catch (Throwable closeError) {
                                    parseError.addSuppressed(closeError);
                                }
                                throw parseError;
                            }
                            reader.close();
                        } catch (Throwable readError) { // Probably not what this actually may catch, but that is what I determined of it.
                            try {
                                inputstream.close();
                            } catch (Throwable closeError) {
                                readError.addSuppressed(closeError);
                            }
                            throw readError;
                        }
                        inputstream.close();
                    }
                } catch (Throwable e) {
                    CorrosiveSculk.LOGGER.error("Failed to read {} in data pack {}",
                            //$ resourcelocation
                            ResourceLocation.fromNamespaceAndPath
                                    (namespace, "sculk_damaging_entities.json"), pack.packId(), e);
                }
            }
        }
        this.SCULK_DAMAGING_ENTITIES = builder.build();
        //? if forgebase
        return null;
    }

    //? if forgebase {
    @Override
    protected void apply(Void v, ResourceManager resourceManager, ProfilerFiller profiler) {}
    //?}

    public int getEntitySculkDamage(Entity entity) {
        return getEntitySculkDamage(/*? if forgebase {*/ForgeRegistries.ENTITY_TYPES/*?} else {*//*BuiltInRegistries.ENTITY_TYPE*//*?}*/.getKey(entity.getType()));
    }

    public int getEntitySculkDamage(ResourceLocation entity) {
        return SCULK_DAMAGING_ENTITIES.getOrDefault(entity,0);
    }
}
