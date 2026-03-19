package model;

// model pentru tabelul produse
public class Produs {
    private int id;
    private String nume;
    private double pret;
    private int stoc;
    private int categorieId;

    public Produs(int id, String nume, double pret, int stoc, int categorieId) {
        this.id = id;
        this.nume = nume;
        this.pret = pret;
        this.stoc = stoc;
        this.categorieId = categorieId;
    }

    public int getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public double getPret() {
        return pret;
    }

    public int getStoc() {
        return stoc;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public void setPret(double pret) {
        this.pret = pret;
    }

    public void setStoc(int stoc) {
        this.stoc = stoc;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }
}