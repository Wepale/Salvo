package com.codeoftheweb.salvo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
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
                }})
                .collect(toList()), HttpStatus.OK)
                : new ResponseEntity<>( Arrays.asList("You are not allowed, be honest please"), HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/players")
    public List<Object> getScorePlayers() {
        return playerRepo.findAll()
                .stream()
                .map(Player::toDTOscore)
                .collect(toList());
    }

    @RequestMapping(value = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> signUpPlayer(@RequestBody Player player) {
        if (player.getUserName().isEmpty() || player.getPassword().isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.LENGTH_REQUIRED);
        }
        if (playerRepo.findByUserName(player.getUserName()).isPresent()) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepo.save(player);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
    public ResponseEntity<Map<String, Object>> createGame(@PathVariable Long gameId, Authentication authentication) {
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
                    put("gamePlayers", gp.get()
                            .getShips()
                            .stream()
                            .map(Ship::toDTO)); }}, HttpStatus.CREATED)
                : new ResponseEntity<> (new LinkedHashMap<String, Object>() {{
                    put("error", "Game don´t exist"); }}, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/games/players/{gpId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShips(@PathVariable Long gpId, @RequestBody List<Ship> ships, Authentication authentication) {
        Optional<GamePlayer> gp = gpRepo.findById(gpId);
        Optional<Player> player = getPlayerOptional(authentication);

        if (!player.isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "You must be login first");
            }}, HttpStatus.UNAUTHORIZED);
        }

        if (!gp.isPresent()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The GamePlayer doesn´t exist");
            }}, HttpStatus.UNAUTHORIZED);
        }

        if (gp.get().getPlayer().getId() != player.get().getId()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The current user is not the GamePlayer the ID references");
            }}, HttpStatus.UNAUTHORIZED);
        }

        if(!gp.get().getShips().isEmpty()) {
            return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
                put("error", "The current user already has ships placed");
            }}, HttpStatus.FORBIDDEN);
        }

        ships.forEach(ship -> {
                    gp.get().addShip(ship);
                    shipRepo.save(ship);
                });
        return new ResponseEntity<>(new LinkedHashMap<String, Object>(){{
            put("succes", "Ships saved");
        }}, HttpStatus.CREATED);
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

//    private Map<String, Object> mapPlayerinfo(Player player) {
//        return new LinkedHashMap<String, Object>() {{
//            put("id", player.getId());
//            put("email", player.getUserName());
//        }};
//    }
//
//    private List<Object> mapGamePlayersinfo(Set<GamePlayer> gps) {
//        return gps.stream()
//                .map(gp -> new LinkedHashMap<String, Object>() {{
//                    put("id", gp.getId());
//                    put("player", gp.getPlayer().toDTO());
//                }})
//                .collect(toList());
//    }
//
//    private List<Object> mapShipsinfo(Set<Ship> ships) {
//        return ships.stream()
//                .map(Ship::toDTO)
//                .collect(toList());
//    }
}