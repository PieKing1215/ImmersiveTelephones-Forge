package me.pieking1215.immersive_telephones.client;

@SuppressWarnings("unused")
public interface ICustomPoseHandler {

    void setHoldingPhoneRightHand(boolean holdingPhone);
    void setHoldingPhoneLeftHand(boolean holdingPhone);
    boolean getHoldingPhoneMainHand();
    boolean getHoldingPhoneOffHand();

}
