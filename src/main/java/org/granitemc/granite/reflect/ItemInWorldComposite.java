package org.granitemc.granite.reflect;

/*****************************************************************************************
 * License (MIT)
 *
 * Copyright (c) 2014. Granite Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ****************************************************************************************/

import org.granitemc.granite.api.Granite;
import org.granitemc.granite.api.Player;
import org.granitemc.granite.api.block.Block;
import org.granitemc.granite.api.block.BlockType;
import org.granitemc.granite.api.block.BlockTypes;
import org.granitemc.granite.api.event.block.BlockPlaceEvent;
import org.granitemc.granite.api.world.World;
import org.granitemc.granite.block.GraniteBlockType;
import org.granitemc.granite.entity.player.GranitePlayer;
import org.granitemc.granite.reflect.composite.Hook;
import org.granitemc.granite.reflect.composite.HookListener;
import org.granitemc.granite.reflect.composite.ProxyComposite;
import org.granitemc.granite.utils.Mappings;
import org.granitemc.granite.utils.MinecraftUtils;
import org.granitemc.granite.world.GraniteWorld;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ItemInWorldComposite extends ProxyComposite {
    public ItemInWorldComposite(Object world) {
        super(Mappings.getClass("n.m.server.management.ItemInWorldManager"), new Class[]{
                Mappings.getClass("n.m.world.World"),
        }, world);

        // Holy mother of method signature
        addHook("activateBlockOrUseItem(n.m.entity.player.EntityPlayer;n.m.world.World;n.m.item.ItemStack;n.m.util.ChunkCoordinates;net.minecraft.block.Direction;float;float;float)", new HookListener() {

            private Field sdfjksdfhjksdfhjk;

            @Override
            public Object activate(Object self, Method method, Method proxyCallback, Hook hook, Object[] args) {
                if (GraniteServerComposite.instance.isOnServerThread()) {
                    // TODO: change to ItemStack class (when available and working)
                    Object itemStack = args[2];
                    Object item = Mappings.invoke(itemStack, "n.m.item.ItemStack", "getItem");

                    if (Mappings.getClass("n.m.item.ItemBlock").isInstance(item)) {
                        Player p = (Player) MinecraftUtils.wrap(args[0]);

                        World w = (World) MinecraftUtils.wrap(args[1]);

                        Object chunkCoordinates = args[3];
                        int preX = (int) Mappings.invoke(chunkCoordinates, "n.m.util.ChunkCoordinates", "getX");
                        int preY = (int) Mappings.invoke(chunkCoordinates, "n.m.util.ChunkCoordinates", "getY");
                        int preZ = (int) Mappings.invoke(chunkCoordinates, "n.m.util.ChunkCoordinates", "getZ");

                        int x = preX;
                        int y = preY;
                        int z = preZ;

                        Block oldBlock = w.getBlock(x, y, z);

                        int direction = ((Enum) args[4]).ordinal();

                        if (oldBlock.getType().typeEquals(BlockTypes.snow_layer) && oldBlock.getType().getMetadata("layers") == 1) {
                            direction = 1;
                        } else if (!(boolean) Mappings.invoke(
                                ((GraniteBlockType) oldBlock.getType()).getBlockObject(),
                                "n.m.block.Block",
                                "isReplaceable(n.m.world.World;n.m.util.ChunkCoordinates)",
                                ((GraniteWorld) w).parent,
                                MinecraftUtils.createChunkCoordinates(x, y, z)
                        )) {
                            switch (direction) {
                                case 0:
                                    y--;
                                    break;
                                case 1:
                                    y++;
                                    break;
                                case 2:
                                    z--;
                                    break;
                                case 3:
                                    z++;
                                    break;
                                case 4:
                                    x--;
                                    break;
                                case 5:
                                    x++;
                                    break;
                            }
                        }

                        BlockType oldBlockType = w.getBlock(x, y, z).getType();

                        try {
                            proxyCallback.invoke(self, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        Block b = w.getBlock(x, y, z);
                        if (((GraniteBlockType) b.getType()).parent != null && !b.getType().typeEquals(BlockTypes.air)) {
                            BlockPlaceEvent event = new BlockPlaceEvent(b, p);
                            Granite.getEventQueue().fireEvent(event);

                            if (event.isCancelled()) {
                                b.setType(oldBlockType);
                                ((GranitePlayer) p).sendBlockUpdate(b);
                            }
                        }

                        hook.setWasHandled(true);
                    }
                }
                return null;
            }
        });
    }
}
