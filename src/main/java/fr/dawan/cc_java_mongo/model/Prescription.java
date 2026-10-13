
package fr.dawan.cc_java_mongo.model;

/**
 * Représente la prescription d'un médicament dans une consultation.
 */
public class Prescription {

    private String medicamentId;

    private int nbPrises;

    public Prescription() {
    }

    public Prescription(String medicamentId, int nbPrises) {
        this.medicamentId = medicamentId;
        this.nbPrises = nbPrises;
    }

    public String getMedicamentId() {
        return medicamentId;
    }

    public void setMedicamentId(String medicamentId) {
        this.medicamentId = medicamentId;
    }

    public int getNbPrises() {
        return nbPrises;
    }

    public void setNbPrises(int nbPrises) {
        this.nbPrises = nbPrises;
    }
}

