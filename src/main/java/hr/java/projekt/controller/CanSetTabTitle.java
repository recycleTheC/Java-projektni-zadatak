package hr.java.projekt.controller;

import javafx.scene.control.Tab;

public interface CanSetTabTitle {
    void storeTabReference(Tab tab);

    void setTabTitle(String title);
}
