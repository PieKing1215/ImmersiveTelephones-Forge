package me.pieking1215.immersive_telephones.common.tile_entity;

import java.util.Optional;

public interface ICallable extends IHasID {

    /**
     * Run when another device dials this one
     * @param dialedBy The device that is calling this instance
     */
    void onDialed(ICallable dialedBy);

    /**
     * May be run by another device to check that this one is still calling it
     * (eg. if the other device plays a sound while being called, they can check if it should stop by calling this)
     * @param other The device that wants to know if this one is still calling it
     * @return true if this device still "wants" to call other, false otherwise
     */
    boolean isStillCalling(ICallable other);

    /**
     * Run when another device accepts/answers your call
     * @param answerer The device that answered your call
     */
    void onAnsweredCall(ICallable answerer);

    /**
     * Run when another device is added to the current call
     * @param whoAdded The device that is adding the new device
     * @param added The new device
     */
    void onAddedToCall(ICallable whoAdded, ICallable added);

    /**
     * Run when another device leaves a call that contains this instance
     * @param leaver The device that left
     */
    void onLeftCall(ICallable leaver);

    default <T> Optional<T> getFunctionality(Class<T> type){
        //noinspection unchecked
        return type.isAssignableFrom(this.getClass()) ? Optional.of((T)this) : Optional.empty();
    }

}
