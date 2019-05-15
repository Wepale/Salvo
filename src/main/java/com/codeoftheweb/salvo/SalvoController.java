package com.codeoftheweb.salvo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private GamePlayerRepository gpRepo;

    @Autowired
    private ShipRepository shipRepo;

    @Autowired
    private SalvoRepository salvoRepo;

    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Optional<Player> player = getPlayerOptional(authentication);
        return new LinkedHashMap<String, Object>() {{
            put("player", player.map(Player::toDTO).orElse(null));
            put("games", gameRepo.findAll()
                    .stream()
                    .map(game -> makeGameInfo(game))
                    .collect(toList()));
        }};
    }
        //Change if for map Optional
    @RequestMapping(value = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(@RequestBody Game game, Authentication authentication){
        Optional<Player> player = getPlayerOptional(authentication);
        if (player.isPresent()) {
            GamePlayer gp = new GamePlayer(player.get(), game);
            gameRepo.save(game);
            gpRepo.save(gp);
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("gpID", gp.getId());
            }}, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "You must be login first");
            }}, HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping("/game_view/{gpId}")
    public ResponseEntity<List<Object>> findGame(@PathVariable Long gpId, Authentication authentication) {
        Optional<Player> player = getPlayerOptional(authentication);
        Optional<GamePlayer> gp = gpRepo.findById(gpId);
        return gp.isPresent() && gp.get().getPlayer().equals(player.orElse(null))
                ? new ResponseEntity<> (gameRepo.findAll()
                .stream()
                .filter(g -> g.getGPbyID(gpId).isPresent())
                .map(game -> new LinkedHashMap<String, Object>() {{
                    put("gameId", game.getId());
                    put("gamePlayers", game.getGamePlayersDTO());
                    put("ships", game.getGPbyID(gpId)
                            .get()
                            .getShips()
                            .stream()
                            .map(Ship::toDTO));
                    put("salvoes", game.getGamePlayers()
                            .stream()
                            .flatMap(gp -> gp.getSalvos()
                                    .stream()
                                    .map(Salvo::toDTO)));
                    put("hitsOnEnemy", getHitsOnEnemyShips(game, gp));
                    put("hitsOnMe", getHitsOnEnemyShips(game, game.getGamePlayers()
                            .stream()
                            .filter(gamePlayer -> gamePlayer.getId() != gp.get().getId())
                            .findFirst())); //The other game player
                    put("opponentHasShips", game.getGamePlayers()
                            .stream()
                            .filter(gameP -> gameP.getId() != gpId)
                            .findFirst()
                            .map(gameP -> !gameP.getShips().isEmpty())
                            .orElse(false));
                }})
                .collect(toList()), HttpStatus.OK)
                : new ResponseEntity<>( Arrays.asList(new LinkedHashMap<String, Object>() {{
            put("error", "This is not your game");
        }}), HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/players")
    public List<Object> getScorePlayers() {
        return playerRepo.findAll()
                .stream()
                .map(Player::toDTOscore)
                .collect(toList());
    }

    @RequestMapping(value = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> signUpPlayer(@RequestBody Player player) {
        if (player.getUserName().isEmpty() || player.getPassword().isEmpty()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "Missing data");
            }}, HttpStatus.LENGTH_REQUIRED);
        }
        if (playerRepo.findByUserName(player.getUserName()).isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "Name already in use");
            }}, HttpStatus.FORBIDDEN);
        }

        playerRepo.save(new Player(player.getUserName(), PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(player.getPassword())));
        return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
            put("error", "All OK, player created");
        }}, HttpStatus.CREATED);

    }

    @RequestMapping("/games/{gameId}/players")
    public ResponseEntity<Map<String, Object>> getGame(@PathVariable Long gameId) {
        Optional<Game> game = gameRepo.findById(gameId);
        return game.isPresent()
                ? new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
                    put("gamePlayers", game.get()
                            .getGamePlayers()
                            .stream()
                            .map(GamePlayer::toDTOGameView)); }}, HttpStatus.CREATED)
                : new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
                    put("error", "Game don´t exist"); }}, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/games/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {
        Optional<Game> game = gameRepo.findById(gameId);
        Optional<Player> player = getPlayerOptional(authentication);

        if (!player.isPresent()){
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "You must be login first");
            }}, HttpStatus.UNAUTHORIZED);
        }

        if (!game.isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The game doesn´t exist");
            }}, HttpStatus.FORBIDDEN);
        }

        if (game.get().getGamePlayers().size() > 1) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "Game is full");
            }}, HttpStatus.FORBIDDEN);
        }

        GamePlayer gp = new GamePlayer(player.get(), game.get());
        gpRepo.save(gp);
        return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
            put("gpID", gp.getId());
        }}, HttpStatus.CREATED);
    }

    @RequestMapping("/games/players/{gpId}/ships")
    public ResponseEntity<Map<String, Object>> getShips(@PathVariable Long gpId) {
        Optional<GamePlayer> gp = gpRepo.findById(gpId);
        return gp.isPresent()
                ? new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
                    put("ships", gp.get()
                            .getShips()
                            .stream()
                            .map(Ship::toDTO)); }}, HttpStatus.CREATED)
                : new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
                    put("error", "Game player doesn´t exist"); }}, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/games/players/{gpId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveShips(@PathVariable Long gpId, @RequestBody List<Ship> ships, Authentication authentication) {
        Optional<GamePlayer> gp = gpRepo.findById(gpId);
        Optional<Player> player = getPlayerOptional(authentication);

        if (checkPlayerAndGP(gp, player).getStatusCode() != HttpStatus.OK) {
            return checkPlayerAndGP(gp, player);
        }

        if(!gp.get().getShips().isEmpty()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The current user already has ships placed");
            }}, HttpStatus.FORBIDDEN);
        }

        if (checkShips(ships)){
            ships.forEach(ship -> {
                gp.get().addShip(ship);
                shipRepo.save(ship);
            });
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("succes", "Ships saved");
            }}, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The ships are not correct");
            }}, HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping("/games/players/{gpId}/salvoes")
    public ResponseEntity<Map<String, Object>> getSalvoes(@PathVariable Long gpId) {
        Optional<GamePlayer> gp = gpRepo.findById(gpId);
        return gp.isPresent()
                ? new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
            put("salvos", gp.get()
                    .getSalvos()
                    .stream()
                    .map(Salvo::toDTO)); }}, HttpStatus.CREATED)
                : new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
            put("error", "Game player doesn´t exist"); }}, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "games/players/{gpId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveSalvos(@PathVariable Long gpId, @RequestBody Salvo salvo, Authentication authentication) {
        Optional<GamePlayer> gp = gpRepo.findById(gpId);
        Optional<Player> player = getPlayerOptional(authentication);

        if (checkPlayerAndGP(gp, player).getStatusCode() != HttpStatus.OK) {
            return checkPlayerAndGP(gp, player);
        }

        Optional<GamePlayer> otherGp = gp.get()
                .getGame()
                .getGamePlayers()
                .stream()
                .filter(gameP -> gameP.getId() != gp.get().getId()).findFirst();

        if (!otherGp.isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "Wait for another player");
            }}, HttpStatus.FORBIDDEN);
        }

        if (gp.get().getSalvos().size() > otherGp.get().getSalvos().size()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "Your opponent has to shoot first");
            }}, HttpStatus.FORBIDDEN);
        }

        if (salvoIsCorrect(salvo, gp.get())) {
            gp.get().addSalvo(salvo);
            salvoRepo.save(salvo);
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("succes", "Salvo saved");
            }}, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The salvo is not correct");
            }}, HttpStatus.FORBIDDEN);
        }
    }

    private boolean salvoIsCorrect(Salvo salvo, GamePlayer gp){
        return salvo.getTurn() - 1 == gp.getSalvos()
                .stream()
                .mapToInt(Salvo::getTurn)
                .max()
                .orElse(0) //Highest turn on salvos
                && salvo.getLocations().size() == 5
                && correctLocations(salvo.getLocations());
    }

    private ResponseEntity<Map<String, Object>> checkPlayerAndGP(Optional<GamePlayer> gp, Optional<Player> player) {
        if (!player.isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>() {{
                put("error", "You must be login first");
            }}, HttpStatus.UNAUTHORIZED);
        }

        if (!gp.isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>() {{
                put("error", "The GamePlayer doesn´t exist");
            }}, HttpStatus.UNAUTHORIZED);
        }

        if (gp.get().getPlayer().getId() != player.get().getId()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>() {{
                put("error", "The current user is not the GamePlayer the ID references");
            }}, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new LinkedHashMap<String, Object>() {{
            put("error", "All OK");
        }}, HttpStatus.OK);
    }

        private Optional<Player> getPlayerOptional(Authentication authentication) {
        return authentication == null ? Optional.empty() : playerRepo.findByUserName(authentication.getName());
    }

    private Map<String, Object> makeGameInfo(Game game) {
        return new LinkedHashMap<String, Object>() {{
            put("id", game.getId());
            put("created", game.getDate());
            put("gamePlayers", game.getGamePlayers()
                    .stream()
                    .map(GamePlayer::toDTOGame)
                    .collect(toList()));
        }};
    }

    private boolean correctShipSize(Ship ship) {
        switch (ship.getType()) {
            case "carrier":
                return ship.getLocation().size() == 5;
            case "battleship":
                return ship.getLocation().size() == 4;
            case "submarine":
                return ship.getLocation().size() == 3;
            case "destroyer":
                return ship.getLocation().size() == 3;
            case "patrolBoat":
                return ship.getLocation().size() == 2;
            default:
                return false; // Incorrect type
        }
    }

    private Map<String, String> possibleLocations() {
        return new LinkedHashMap<String, String>() {{
            put("A", "1");
            put("B", "2");
            put("C", "3");
            put("D", "4");
            put("E", "5");
            put("F", "6");
            put("G", "7");
            put("H", "8");
            put("I", "9");
            put("J", "10");
        }};

    }

    private boolean correctLocations(List<String> locations) {
        return locations
                .stream()
                .allMatch(location -> possibleLocations().containsKey(location.substring(0, 1))
                        && possibleLocations().containsValue(location.substring(1)))                  // Locations are on range
                && new HashSet<>(locations).size() == locations.size();                             // Locations are not repeated
    }

    private boolean correctShipsLocations(Ship ship) {
        if (!correctLocations(ship.getLocation())) {
            return false;
        }

        List<Integer> numbers = ship.getLocation()
                .stream()
                .map(location -> Integer.valueOf(location.substring(1))).collect(toList());
        List<String> letters = ship.getLocation()
                .stream()
                .map(location -> location.substring(0,1)).collect(toList());
        Supplier<IntStream> indexes = () -> IntStream.range(1, numbers.size());

        boolean sameNumbers = indexes.get().allMatch(i -> numbers.get(i) == numbers.get(i-1));
        boolean sameLetters= indexes.get().allMatch(i -> letters.get(i).equals(letters.get(i-1)));

        if (sameNumbers && !sameLetters) { //Vertical ship
            List<Integer> collect = letters.stream().map(letter -> Integer.valueOf(possibleLocations().get(letter))).collect(toList());
            return indexes.get().allMatch(i -> collect.get(i) == (collect.get(i-1) + 1));
        } else if (sameLetters && !sameNumbers){ // Horizontal ship
            return indexes.get().allMatch(i -> numbers.get(i) == numbers.get(i-1) + 1);
        } else { // Locations are not adjacent
            return false;
        }
    }

    private boolean checkShips(List<Ship> ships) {
        return ships.size() == 5
                && ships.stream().allMatch(this::correctShipSize)
                && ships.stream().allMatch(this::correctShipsLocations);
    }

    private List<Map<String, Object>> getHitsOnEnemyShips(Game game, Optional<GamePlayer> gp) {
        Set<Ship> enemyShips = game.getGamePlayers()
                .stream()
                .filter(gamePlayer -> gamePlayer.getId() != gp.map(GamePlayer::getId).orElse(null))
                .findFirst()
                .map(GamePlayer::getShips)
                .orElse(null);

        return enemyShips != null && gp.isPresent()
                ? gp.get().getSalvos()
                .stream()
                .map(Salvo::getTurn)
                .sorted(Comparator.reverseOrder())
                .map(turn -> new LinkedHashMap<String, Object>() {{
                    put("turn", turn);
                    put("ships", enemyShips
                            .stream()
                            .filter(ship -> gp.get().getSalvos()
                                    .stream()
                                    .filter(salvo -> salvo.getTurn() == turn)
                                    .flatMap(salvo -> salvo.getLocations().stream())
                                    .anyMatch(shoot -> ship.getLocation().contains(shoot)))
                            .map(ship -> new LinkedHashMap<String, Object>() {{
                                put("type", ship.getType());
                                put("hitLocations", ship.getLocation()
                                        .stream()
                                        .flatMap(location -> gp.get().getSalvos()
                                                .stream()
                                                .filter(salvo -> salvo.getTurn() == turn)
                                                .flatMap(salvo -> salvo.getLocations().stream())
                                                .filter(shoot -> shoot.equals(location)))
                                        .collect(toList()));
                            }}).collect(toList()));
                }}).collect(toList())
                : Arrays.asList();
    }

}