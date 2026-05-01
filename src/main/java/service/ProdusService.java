package service;

import entity.Produs;
import repository.ProdusRepository;

import java.util.List;

public class ProdusService {

    private final ProdusRepository produsRepository = new ProdusRepository();

    public List<Produs> getProduseByCategorieId(int categorieId) {
        return produsRepository.findByCategorieId(categorieId);
    }

    public void adaugaProdus(String nume, double pret, int stoc, int categorieId) {
        valideazaProdus(nume, pret, stoc);
        produsRepository.save(nume.trim(), pret, stoc, categorieId);
    }

    public void modificaProdus(int id, String nume, double pret, int stoc) {
        valideazaProdus(nume, pret, stoc);
        produsRepository.update(id, nume.trim(), pret, stoc);
    }

    public void stergeProdus(int id) {
        produsRepository.delete(id);
    }

    private void valideazaProdus(String nume, double pret, int stoc) {
        if (nume == null || nume.trim().isEmpty()) {
            throw new RuntimeException("numele nu poate fi gol");
        }

        if (pret <= 0) {
            throw new RuntimeException("pretul trebuie sa fie mai mare ca 0");
        }

        if (stoc < 0) {
            throw new RuntimeException("stocul nu poate fi negativ");
        }
    }
}