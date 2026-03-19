package dao;

import db.DatabaseManager;
import model.Produs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// clasa pentru operatii pe tabelul produse
public class ProdusDAO {

    public List<Produs> getProduseByCategorieId(int categorieId) {
        List<Produs> produse = new ArrayList<>();
        String sql = "select id, nume, pret, stoc, categorie_id from produse where categorie_id = ? order by id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categorieId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Produs produs = new Produs(
                            resultSet.getInt("id"),
                            resultSet.getString("nume"),
                            resultSet.getDouble("pret"),
                            resultSet.getInt("stoc"),
                            resultSet.getInt("categorie_id")
                    );
                    produse.add(produs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la citirea produselor");
        }

        return produse;
    }

    public void addProdus(Produs produs) {
        String sql = "insert into produse(nume, pret, stoc, categorie_id) values (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, produs.getNume());
            statement.setDouble(2, produs.getPret());
            statement.setInt(3, produs.getStoc());
            statement.setInt(4, produs.getCategorieId());

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la adaugarea produsului");
        }
    }

    public void updateProdus(Produs produs) {
        String sql = "update produse set nume = ?, pret = ?, stoc = ? where id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, produs.getNume());
            statement.setDouble(2, produs.getPret());
            statement.setInt(3, produs.getStoc());
            statement.setInt(4, produs.getId());

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la modificarea produsului");
        }
    }

    public void deleteProdus(int id) {
        String sql = "delete from produse where id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la stergerea produsului");
        }
    }
}