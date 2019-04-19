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

    private Optional<Player> getPlayerOptional(Authentication authentication) {
        return authentication == null ? Optional.empty() : playerRepo.findByUserName(authentication.getName());
    }

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

    private Map<String, Object> mapPlayerinfo(Player player) {
        return new LinkedHashMap<String, Object>() {{
            put("id", player.getId());
            put("email", player.getUserName());
        }};
    }

    private List<Object> mapGamePlayersinfo(Set<GamePlayer> gps) {
        return gps.stream()
                .map(gp -> new LinkedHashMap<String, Object>() {{
                    put("id", gp.getId());
                    put("player", gp.getPlayer().toDTO());
                }})
                .collect(toList());
    }

    private List<Object> mapShipsinfo(Set<Ship> ships) {
        return ships.stream()
                .map(Ship::toDTO)
                .collect(toList());
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

    @RequestMapping("/players")
    public List<Object> getScorePlayers() {
        return playerRepo.findAll()
                .stream()
                .map(Player::toDTOscore)
                .collect(toList());
    }
}