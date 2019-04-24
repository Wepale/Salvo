package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<Score> scores = new HashSet<>();

    private String userName;
    private String password;

    public Player() { }
//    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    public Player(String userName, String password) {
        this.userName = userName;
        this.password = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password);
    }

    // Get Methods
    public String getUserName(){
        return userName;
    }

    public long getId() {
        return this.id;
    }

    public Set<Score> getScores() {
        return this.scores;
    }

    public void setPassword(String pass){
        password = pass;
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers() {
        return this.gamePlayers;
    }

    @JsonIgnore
    public List<Object> getGames() {
        return gamePlayers.stream().map(gp -> gp.getGame().toDTO()).collect(toList());
    }

    public void addScore(Score score) {
        score.setPlayer(this);
        scores.add(score);
    }

    public String getPassword(){
        return password;
    }

    public String toString() {
        return userName;
    }

    public Map<String, Object> toDTO() {
            return new LinkedHashMap<String, Object>() {{
                put("id", id);
                put("name", userName);
            }};
    }

    public Map<String, Object> toDTOscore() {
        return new LinkedHashMap<String, Object>(){{
            put("id", id);
            put("name", userName);
            put("scores", scores);
        }};
    }
}