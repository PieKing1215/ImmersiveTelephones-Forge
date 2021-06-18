package me.pieking1215.immersive_telephones.common.block.router;

public interface ICapacityHandler {

    <T> int getCapacity(Class<T> type);

}
