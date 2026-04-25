package dao;

import db.DatabaseManager;
import model.Categorie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// clasa pentru operatii pe tabelul categorii
public class CategorieDAO {

    public List<Categorie> getAllCategorii() {
        List<Categorie> categorii = new ArrayList<>();
        String sql = "select id, nume from categorii order by id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Categorie categorie = new Categorie(
                        resultSet.getInt("id"),
                        resultSet.getString("nume")
                );
                categorii.add(categorie);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la citirea categoriilor");
        }

        return categorii;
    }
}