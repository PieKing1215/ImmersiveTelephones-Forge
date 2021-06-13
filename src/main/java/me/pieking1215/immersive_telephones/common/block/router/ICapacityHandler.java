package me.pieking1215.immersive_telephones.common.block.router;

import me.pieking1215.immersive_telephones.common.tile_entity.IHasID;

public interface ICapacityHandler {

    <T extends IHasID> int getCapacity(Class<T> type);

}
