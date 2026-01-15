package com.jpl.smtm.manager;

import java.awt.EventQueue;
import com.jpl.smtm.manager.controller.ManagerController;
import com.jpl.smtm.manager.model.ManagerModel;
import com.jpl.smtm.manager.view.ManagerView;

public class ManagerApp {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ManagerModel model = new ManagerModel();
                    ManagerView view = new ManagerView();
                    new ManagerController(model, view);
                    view.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}