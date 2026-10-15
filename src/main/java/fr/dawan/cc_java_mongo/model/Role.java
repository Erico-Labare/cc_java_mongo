package fr.dawan.cc_java_mongo.model;

/**
 * Énumération des rôles utilisateurs dans l'application.
 * <p>
 * Cette énumération définit les trois rôles métier principaux :
 * <ul>
 * <li>{@link #PATIENT} - Patient, peut accéder à son propre dossier</li>
 * <li>{@link #MEDECIN} - Médecin, peut gérer les consultations et documents</li>
 * <li>{@link #ADMIN} - Administrateur, peut gérer les comptes utilisateurs</li>
 * </ul>
 * </p>
 */
public enum Role {

    /**
     * Rôle patient.
     * Un patient peut consulter son propre dossier et ses consultations,
     * mais ne peut pas accéder aux données d'autres patients.
     */
    PATIENT,

    /**
     * Rôle médecin.
     * Un médecin peut créer et gérer les consultations,
     * et accéder aux dossiers des patients qu'il consulte.
     */
    MEDECIN,

    /**
     * Rôle administrateur.
     * Un administrateur peut gérer les comptes utilisateurs
     * et les rôles, mais n'a pas accès automatiquement aux données médicales.
     */
    ADMIN
}
