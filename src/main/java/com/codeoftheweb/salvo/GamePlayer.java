package com.codeoftheweb.salvo;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private Set<Salvo> salvos = new HashSet<>();

    private String joinDate;

    public GamePlayer() { }

    public GamePlayer(Player player, Game game) {
        this.joinDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
        this.player = player;
        this.game = game;
    }

    // Get & Set Methods

    public String getDate() {
        return joinDate;
    }

    @JsonIgnore
    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public Set<Ship> getShips() {
        return this.ships;
    }

    @JsonIgnore
    public Set<Salvo> getSalvos() {
        return this.salvos;
    }

    public void setDate(String date) {
        this.joinDate = date;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String toString() {
        return joinDate + " " + player + " " + game;
    }

    public void addShip(Ship ship) {
        ship.setGamePlayer(this);
        ships.add(ship);
    }

    public void addSalvo(Salvo salvo) {
        salvo.setGamePlayer(this);
        salvos.add(salvo);
    }

    @JsonIgnore
    public Score getScore(){
       return player.getScores()
                .stream()
                .flatMap(pScore -> game.getScores()
                        .stream()
                        .filter(gScore -> gScore.getId() == pScore.getId()))
               .findFirst()
               .orElse(null);
    }

    public Map<String, Object> toDTOAll() {
        return new LinkedHashMap<String, Object>(){{
            put("id", getId());
            put("player", getPlayer().toDTO());
            put("game", getGame().toDTO());
            put("ships", getShips()
                    .stream()
                    .map(Ship::toDTO)
                    .collect(toList()));
        }};
    }

    public Map<String, Object> toDTOGameView() {
        return new LinkedHashMap<String, Object>(){{
            put("id", getId());
            put("player", getPlayer().toDTO());
        }};
    }

    public Map<String, Object> toDTOGame() {
        return new LinkedHashMap<String, Object>(){{
            put("id", getId());
            put("player", getPlayer().toDTO());
            put("score", getScore());
        }};
    }

}