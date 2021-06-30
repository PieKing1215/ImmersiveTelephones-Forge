package me.pieking1215.immersive_telephones.common.block;

public interface IHasID {

    String getID();

    default boolean matches(String query){
        return query.equals(getID());
    }

}
