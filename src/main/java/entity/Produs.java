package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "produse")
public class Produs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nume", nullable = false)
    private String nume;

    @Column(name = "pret", nullable = false)
    private Double pret;

    @Column(name = "stoc", nullable = false)
    private Integer stoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    public Produs() {
    }

    public Produs(String nume, Double pret, Integer stoc, Categorie categorie) {
        this.nume = nume;
        this.pret = pret;
        this.stoc = stoc;
        this.categorie = categorie;
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

    public Double getPret() {
        return pret;
    }

    public void setPret(Double pret) {
        this.pret = pret;
    }

    public Integer getStoc() {
        return stoc;
    }

    public void setStoc(Integer stoc) {
        this.stoc = stoc;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }
}