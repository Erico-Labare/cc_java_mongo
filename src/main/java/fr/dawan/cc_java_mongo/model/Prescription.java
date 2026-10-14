
package fr.dawan.cc_java_mongo.model;

/**
 * Représente la prescription d'un médicament dans une consultation.
 * <p>
 * Cette classe est un sous-document embarqué dans {@link Consultation}.
 * Elle associe un médicament à un nombre de prises prescrites.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Consultation
 * @see fr.dawan.cc_java_mongo.model.Medicament
 */
public class Prescription {

    private String medicamentId;

    private int nbPrises;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON.
     */
    public Prescription() {
    }

    /**
     * Construit une prescription avec un médicament et un nombre de prises.
     *
     * @param medicamentId l'identifiant du médicament prescrit
     * @param nbPrises le nombre de prises prescrites
     */
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

