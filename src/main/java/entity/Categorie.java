package entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorii")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nume", nullable = false, unique = true)
    private String nume;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Produs> produse = new ArrayList<>();

    public Categorie() {
    }

    public Categorie(String nume) {
        this.nume = nume;
    }

    public Integer getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public List<Produs> getProduse() {
        return produse;
    }

    public void setProduse(List<Produs> produse) {
        this.produse = produse;
    }

    public void adaugaProdus(Produs produs) {
        produse.add(produs);
        produs.setCategorie(this);
    }

    public void stergeProdus(Produs produs) {
        produse.remove(produs);
        produs.setCategorie(null);
    }

    @Override
    public String toString() {
        return nume;
    }
}