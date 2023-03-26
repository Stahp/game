package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import fr.umontpellier.iut.rails.data.CarteTransport;
import fr.umontpellier.iut.rails.data.Destination;
import fr.umontpellier.iut.rails.data.TypeCarteTransport;
import fr.umontpellier.iut.rails.data.Ville;

public class Joueur {
	public enum CouleurJouer {
		JAUNE, ROUGE, BLEU, VERT, ROSE;
	}

	/**
	 * Jeu auquel le joueur est rattaché
	 */
	private final Jeu jeu;
	/**
	 * Nom du joueur
	 */
	private final String nom;
	/**
	 * CouleurJouer du joueur (pour représentation sur le plateau)
	 */
	private final CouleurJouer couleur;
	/**
	 * Liste des villes sur lesquelles le joueur a construit un port
	 */
	private final List<Ville> ports;
	/**
	 * Liste des routes capturées par le joueur
	 */
	private final List<Route> routes;
	/**
	 * Nombre de pions wagons que le joueur peut encore poser sur le plateau
	 */
	private int nbPionsWagon;
	/**
	 * Nombre de pions wagons que le joueur a dans sa réserve (dans la boîte)
	 */
	private int nbPionsWagonEnReserve;
	/**
	 * Nombre de pions bateaux que le joueur peut encore poser sur le plateau
	 */
	private int nbPionsBateau;
	/**
	 * Nombre de pions bateaux que le joueur a dans sa réserve (dans la boîte)
	 */
	private int nbPionsBateauEnReserve;
	/**
	 * Liste des destinations à réaliser pendant la partie
	 */
	private final List<Destination> destinations;
	/**
	 * Liste des cartes que le joueur a en main
	 */
	private final List<CarteTransport> cartesTransport;
	/**
	 * Liste temporaire de cartes transport que le joueur est en train de jouer pour
	 * payer la capture d'une route ou la construction d'un port
	 */
	private final List<CarteTransport> cartesTransportPosees;
	/**
	 * Score courant du joueur (somme des valeurs des routes capturées, et points
	 * perdus lors des échanges de pions)
	 */
	private int score;
	private int nbPionsMaximum;

	public void ajouterCarteDestination(Destination destination) {
		destinations.add(destination);
	}

	public Joueur(String nom, Jeu jeu, CouleurJouer couleur) {
		this.nom = nom;
		this.jeu = jeu;
		this.couleur = couleur;
		this.ports = new ArrayList<>();
		this.routes = new ArrayList<>();
		this.nbPionsWagon = 0;
		this.nbPionsWagonEnReserve = 25;
		this.nbPionsBateau = 0;
		this.nbPionsBateauEnReserve = 50;
		this.cartesTransport = new ArrayList<>();
		this.cartesTransportPosees = new ArrayList<>();
		this.destinations = new ArrayList<>();
		this.score = 0;
		this.nbPionsMaximum = 60;
	}

	public String getNom() {
		return nom;
	}

	/**
	 * Cette méthode est appelée à tour de rôle pour chacun des joueurs de la
	 * partie. Elle doit réaliser un tour de jeu, pendant lequel le joueur a le
	 * choix entre 5 actions possibles : - piocher des cartes transport (visibles ou
	 * dans la pioche) - échanger des pions wagons ou bateau - prendre de nouvelles
	 * destinations - capturer une route - construire un port
	 */
	void jouerTour() {
		// IMPORTANT : Le corps de cette fonction est à réécrire entièrement
		// Un exemple très simple est donné pour illustrer l'utilisation de certaines
		// méthodes
//		Route route;
		while (true) {
			List<String> routesPossibles = jeu.getRoutesLibres().stream().map(route -> route.getNom()).toList();
			String inputR = choisir("A vous de jouer: ", routesPossibles, null, false);
			boolean f = false;
			// CaptureRoute
			if (inputR.startsWith("R")) { // || input.startsWith("C")
				Route chosenRoute = jeu.getRoutesLibres().stream().filter(route -> route.getNom().equals(inputR))
						.findFirst().get();
				jeu.log("Route Choisie: " + chosenRoute.toString());
				int count = 0;
				while (true) {
					String inputC = choisir(String.format("La route Choisie est: %s", chosenRoute.getNom()),
							cartesTransport.stream().map(carte -> carte.getNom()).toList(), null, false);
					jeu.log(inputC);
					if (inputC.startsWith("C")) { // || input.startsWith("C")
						CarteTransport carteChoisie = cartesTransport.stream()
								.filter(carte -> carte.getNom().equals(inputC)).findFirst().get();
						jeu.log("Carte Choisie: " + carteChoisie.toString());
						if (carteChoisie.getCouleur() == chosenRoute.getCouleur()
								&& ((chosenRoute instanceof RouteMaritime
										&& carteChoisie.getType() == TypeCarteTransport.BATEAU)
										|| (chosenRoute instanceof RouteTerrestre
												&& carteChoisie.getType() == TypeCarteTransport.WAGON))) {

							cartesTransport.remove(carteChoisie);
							cartesTransportPosees.add(carteChoisie);
							count++;
							if (count == chosenRoute.getLongueur()) {
								routes.add(chosenRoute);
								this.score += count;
								f = true;
								break;
							}
						}
					}
				}
				if (f)
					break;
			}
		}

//		List<String> optionsVilles = new ArrayList<>();
//		for (Ville ville : jeu.getPortsLibres()) {
//			optionsVilles.add(ville.nom());
//		}
//		List<Bouton> boutons = Arrays.asList(new Bouton("Montpellier"), new Bouton("Sète"));
//
//		String choix = choisir("Choisissez votre ville préférée", optionsVilles, boutons, true);
//
//		if (choix.equals("")) {
//			log(String.format("%s n'aime aucune ville", toLog()));
//		} else {
//			log(String.format("%s a choisi %s", toLog(), choix));
//		}
	}

	void prendrePossessionDuneRoute() {
		List<Route> routesPossible = new ArrayList<>();
		for (Route route : jeu.getRoutesLibres()) {
			if (this.peutPrendreRoute(route)) {
				routesPossible.add(route);
			}
		}

		List<String> choix = new ArrayList<>();
		Collection<Bouton> boutons = new ArrayList<>();
		for (Route route : routesPossible) {
			String name = route.toString();
			choix.add(name);
			boutons.add(new Bouton(name));
		}
		String input = choisir("Choisissez uen route pour en prendre possession: ", choix, boutons, false);
		jeu.log(input);
//		string input= choisir("Choisissez uen route pour eb prendre possession: ", )
	}

	boolean peutPrendreRoute(Route r) {
		if (r instanceof RouteMaritime) {

			int count = 0;
			if (nbPionsBateau < r.getLongueur())
				return false;
			for (CarteTransport carteTransport : cartesTransport) {
				if (carteTransport.getType() == TypeCarteTransport.BATEAU
						&& carteTransport.getCouleur() == r.getCouleur())
					count++;
			}

			return count >= r.getLongueur();
		} else {

			int count = 0;
			if (nbPionsWagon < r.getLongueur())
				return false;
			for (CarteTransport carteTransport : cartesTransport) {
				if (carteTransport.getType() == TypeCarteTransport.WAGON
						&& carteTransport.getCouleur() == r.getCouleur())
					count++;
			}

			return count >= r.getLongueur();
		}

	}

	void premierTour() {
		// choix destinations
		List<Destination> destinationsTemp = new ArrayList<Destination>();
		List<String> choixDestinations = new ArrayList<String>();
		List<String> destinationsChoisies = new ArrayList<String>();
		List<Bouton> boutons = new ArrayList<Bouton>();
		Arrays.asList(new Bouton("Montpellier"), new Bouton("Sète"));
		for (int i = 0; i < 5; i++) {
			Destination destination = jeu.piocherCarteDestination();
			destinationsTemp.add(destination);
			choixDestinations.add(destination.toString());
			boutons.add(new Bouton(destination.toString()));
		}
		for (int i = 0; i < 3; i++) {
			String choix = choisir("Choisissez votre ville préférée", choixDestinations, boutons, false);
			choixDestinations.remove(choix);
			destinationsChoisies.add(choix);

			// on aurait pu juste recreer cette classe au lien de faire ca !
			Destination dest = destinationsTemp.stream().filter(d -> d.toString().equals(choix)).findFirst().get();
			Bouton bouton = boutons.stream().filter(b -> b.valeur().equals(choix)).findFirst().get();

			destinationsTemp.remove(dest);
			boutons.remove(bouton);
		}

		for (Destination d : destinationsTemp) {
			jeu.replacerCarteDestination(d);
		}
		// 5
		// choix pions
		while (true) {
			int nombreWagons = choisir(
					String.format("Choisissez le nombre de pions Wagon : (minimum de %d, maximum de %d)",
							nbPionsMaximum - nbPionsBateauEnReserve, nbPionsWagonEnReserve),
					nbPionsMaximum - nbPionsBateauEnReserve, nbPionsWagonEnReserve);

			int nombreBateaux = choisir(
					String.format("Choisissez le nombre de pions Bateau : (minimum de %d, maximum de %d)",
							nbPionsMaximum - nbPionsWagonEnReserve, nbPionsBateauEnReserve),
					nbPionsMaximum - nbPionsWagonEnReserve, nbPionsBateauEnReserve);
			if (nombreWagons + nombreBateaux == nbPionsMaximum) {
				this.nbPionsWagon = nombreWagons;
				this.nbPionsBateau = nombreBateaux;
//				this.nbPionsWagonEnReserve = 0;
//				this.nbPionsBateauEnReserve = 0;
				break;
			} else {

				while (true) {
					jeu.prompt(String.format("Le nombre de pions n'est pas égal à %d", nbPionsMaximum),
							new ArrayList<>(), false);
					jeu.lireLigne();
					// si une réponse valide est obtenue, elle est renvoyée
					break;
				}
			}
		}
		// 6 TODO
	}

	/**
	 * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
	 * renvoie le choix du joueur.
	 *
	 * Cette méthode lit les entrées du jeu (`Jeu.lireligne()`) jusqu'à ce qu'un
	 * choix valide (un élément de `choix` ou de `boutons` ou éventuellement la
	 * chaîne vide si l'utilisateur est autorisé à passer) soit reçu. Lorsqu'un
	 * choix valide est obtenu, il est renvoyé par la fonction.
	 *
	 * Exemple d'utilisation pour demander à un joueur de répondre à une question
	 * par "oui" ou "non" :
	 *
	 * ``` List<String> choix = Arrays.asList("Oui", "Non"); String input =
	 * choisir("Voulez-vous faire ceci ?", choix, null, false); ```
	 *
	 * Si par contre on voulait proposer les réponses à l'aide de boutons, on
	 * pourrait utiliser :
	 *
	 * ``` List<Bouton> boutons = Arrays.asList(new Bouton("Un", "1"), new
	 * Bouton("Deux", "2"), new Bouton("Trois", "3")); String input =
	 * choisir("Choisissez un nombre.", null, boutons, false); ```
	 *
	 * @param instruction message à afficher à l'écran pour indiquer au joueur la
	 *                    nature du choix qui est attendu
	 * @param choix       une collection de chaînes de caractères correspondant aux
	 *                    choix valides attendus du joueur
	 * @param boutons     une collection de `Bouton` représentés par deux String
	 *                    (label, valeur) correspondant aux choix valides attendus
	 *                    du joueur qui doivent être représentés par des boutons sur
	 *                    l'interface graphique (le label est affiché sur le bouton,
	 *                    la valeur est ce qui est envoyé au jeu quand le bouton est
	 *                    cliqué)
	 * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
	 *                    faire de choix. S'il est autorisé à passer, c'est la
	 *                    chaîne de caractères vide ("") qui signifie qu'il désire
	 *                    passer.
	 * @return le choix de l'utilisateur (un élement de `choix`, ou la valeur d'un
	 *         élément de `boutons` ou la chaîne vide)
	 */
	public String choisir(String instruction, Collection<String> choix, Collection<Bouton> boutons,
			boolean peutPasser) {
		if (choix == null)
			choix = new ArrayList<>();
		if (boutons == null)
			boutons = new ArrayList<>();

		HashSet<String> choixDistincts = new HashSet<>(choix);
		choixDistincts.addAll(boutons.stream().map(Bouton::valeur).toList());
		if (peutPasser || choixDistincts.isEmpty()) {
			choixDistincts.add("");
		}

		String entree;
		// Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
		while (true) {
			jeu.prompt(instruction, boutons, peutPasser);
			entree = jeu.lireLigne();
			// si une réponse valide est obtenue, elle est renvoyée
			if (choixDistincts.contains(entree)) {
				return entree;
			}
		}
	}

	public int choisir(String instruction, int min, int max) {

		String entree;
		// Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
		while (true) {
			jeu.prompt(instruction, new ArrayList<Bouton>(), false);
			entree = jeu.lireLigne();
			// si une réponse valide est obtenue, elle est renvoyée
			try {
				int val = Integer.parseInt(entree);
				if (val >= min && val <= max)
					return val;
			} catch (NumberFormatException e) {
			}
		}
	}

	/**
	 * Affiche un message dans le log du jeu (visible sur l'interface graphique)
	 *
	 * @param message le message à afficher (peut contenir des balises html pour la
	 *                mise en forme)
	 */
	public void log(String message) {
		jeu.log(message);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add(String.format("=== %s (%d pts) ===", nom, score));
		joiner.add(String.format("  Wagons: %d  Bateaux: %d", nbPionsWagon, nbPionsBateau));
		return joiner.toString();
	}

	/**
	 * @return une chaîne de caractères contenant le nom du joueur, avec des balises
	 *         HTML pour être mis en forme dans le log
	 */
	public String toLog() {
		return String.format("<span class=\"joueur\">%s</span>", nom);
	}

	boolean destinationEstComplete(Destination d) {
		// Cette méthode pour l'instant renvoie false pour que le jeu puisse s'exécuter.
		// À vous de modifier le corps de cette fonction pour qu'elle retourne la valeur
		// attendue.
		return false;
	}

	public int calculerScoreFinal() {
		throw new RuntimeException("Méthode pas encore implémentée !");
	}

	/**
	 * Renvoie une représentation du joueur sous la forme d'un dictionnaire de
	 * valeurs sérialisables (qui sera converti en JSON pour l'envoyer à l'interface
	 * graphique)
	 */
	Map<String, Object> dataMap() {
		return Map.ofEntries(Map.entry("nom", nom), Map.entry("couleur", couleur), Map.entry("score", score),
				Map.entry("pionsWagon", nbPionsWagon), Map.entry("pionsWagonReserve", nbPionsWagonEnReserve),
				Map.entry("pionsBateau", nbPionsBateau), Map.entry("pionsBateauReserve", nbPionsBateauEnReserve),
				Map.entry("destinationsIncompletes",
						destinations.stream().filter(d -> !destinationEstComplete(d)).toList()),
				Map.entry("destinationsCompletes", destinations.stream().filter(this::destinationEstComplete).toList()),
				Map.entry("main", cartesTransport.stream().sorted().toList()),
				Map.entry("inPlay", cartesTransportPosees.stream().sorted().toList()),
				Map.entry("ports", ports.stream().map(Ville::nom).toList()),
				Map.entry("routes", routes.stream().map(Route::getNom).toList()));
	}

	public void initJoueur() {
		// Donner 3 cartes Wagon et 7 cartes Bateau à chaque joueur
		for (int i = 0; i < 3; i++) {
			CarteTransport carteWagon = jeu.piocherCarteWagon();
			if (carteWagon != null) {
				cartesTransport.add(carteWagon);
			}
		}
		for (int i = 0; i < 7; i++) {
			CarteTransport carteBateau = jeu.piocherCarteBateau();
			if (carteBateau != null) {
				cartesTransport.add(carteBateau);
			}
		}

		// Demander au joueur de choisir combien de pions il souhaite garder en début de
		// partie
//        choisir("Choisir le nombre de wagons ")
	}

}
