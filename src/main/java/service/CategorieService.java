package service;

import entity.Categorie;
import repository.CategorieRepository;

import java.util.List;

public class CategorieService {

    private final CategorieRepository categorieRepository = new CategorieRepository();

    public List<Categorie> getAllCategorii() {
        return categorieRepository.findAll();
    }

    public Categorie getCategorieById(int id) {
        return categorieRepository.findById(id);
    }

    public Categorie getCategorieWithProduse(int id) {
        return categorieRepository.findByIdWithProduse(id);
    }

    public void adaugaCategorie(String nume) {
        if (nume == null || nume.trim().isEmpty()) {
            throw new RuntimeException("numele categoriei nu poate fi gol");
        }

        categorieRepository.save(new Categorie(nume.trim()));
    }
}