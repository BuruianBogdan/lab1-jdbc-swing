import config.HibernateUtil;
import ui.Lab4Frame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Lab4Frame frame = new Lab4Frame();
            frame.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown));
    }
}