package com.jpl.smtm.user;

import java.awt.EventQueue;
import com.jpl.smtm.user.controller.UserController;
import com.jpl.smtm.user.model.UserModel;
import com.jpl.smtm.user.view.UserView;

public class UserApp {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Model 생성
                    UserModel model = new UserModel();
                    // 2. View 생성
                    UserView view = new UserView();
                    // 3. Controller 생성
                    new UserController(model, view);
                    view.setVisible(true);
                } 
                
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}