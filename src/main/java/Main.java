import db.DatabaseInitializer;
import ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        new MainFrame().setVisible(true);
    }
}