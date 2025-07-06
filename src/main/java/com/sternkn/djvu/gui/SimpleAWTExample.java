package com.sternkn.djvu.gui;

import java.awt.Frame;
import java.awt.Label;
import java.awt.Button;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SimpleAWTExample {

    public SimpleAWTExample() {
        // Create a new Frame (the main window)
        Frame frame = new Frame("My AWT Application");

        // Create a Label
        Label label = new Label("Click the button!", Label.CENTER);
        label.setBounds(50, 50, 200, 30); // x, y, width, height

        // Create a Button
        Button button = new Button("Click Me");
        button.setBounds(100, 100, 100, 30); // x, y, width, height

        // Add an ActionListener to the button
        button.addActionListener(e -> {
            label.setText("Button Clicked!"); // Change label text on button click
        });

        // Add the label and button to the frame
        frame.add(label);
        frame.add(button);

        // Set the layout manager to null for manual component positioning
        frame.setLayout(null);

        // Set the size of the frame
        frame.setSize(300, 200);

        // Add a WindowListener to handle window closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0); // Terminate the application when the window is closed
            }
        });

        // Make the frame visible
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new SimpleAWTExample(); // Create an instance of the class to run the AWT application
    }
}
