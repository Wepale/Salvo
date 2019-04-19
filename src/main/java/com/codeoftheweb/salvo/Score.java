package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    private Double score;

    private String date;

    public Score(){

    }

    public Score(Double score, String date) {
        this.score= score;
        this.date = date;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public long getId() {
        return this.id;
    }

    public Double getScore() {
        return this.score;
    }

    @JsonIgnore
    public Player getPlayer() {
        return this.player;
    }

    @JsonIgnore
    public Game getGame() {
        return this.game;
    }

    @JsonIgnore
    public String getDate() {
        return this.date;
    }

    @Override
    public String toString() {
        return "Game id: " + game.getId() + " Player: " + player.getUserName() + " Score: " + score;
    }
}
