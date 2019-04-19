package com.codeoftheweb.salvo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String date;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<Score> scores = new HashSet<>();

    public Game() { }

    public Game(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public Set<GamePlayer> getGamePlayers() {
        return this.gamePlayers;
    }

    public Set<Score> getScores() {
        return this.scores;
    }

    public List<Object> getGamePlayersDTO() {
        return gamePlayers.stream().map(GamePlayer::toDTOGameView).collect(toList());
    }

    public Optional<GamePlayer> getGPbyID(Long id){
        return gamePlayers.stream().filter(gp -> gp.getId() == id).findAny();
    }

    public List<Long> getPlayersIds(){
        return gamePlayers.stream().map(gp -> gp.getPlayer().getId()).collect(toList());
    }

    public long getId() {
        return id;
    }

    public String toString() {
        return date;
    }


    public List<Object> getPlayers() {
        return gamePlayers.stream().map(gp -> gp.getPlayer().toDTO()).collect(toList());
    }
    public void addScore(Score score) {
        score.setGame(this);
        scores.add(score);
    }

    public Map<String, Object> toDTO() {
        return new LinkedHashMap<String, Object>(){{
            put("id", getId());
            put("date", getDate());
        }};
    }
}

